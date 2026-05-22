package com.commonplant.garden.plant.service;

import com.commonplant.garden.common.exception.BusinessException;
import com.commonplant.garden.plant.dto.PlantRequest;
import com.commonplant.garden.plant.dto.PlantResponse;
import com.commonplant.garden.plant.entity.Plant;
import com.commonplant.garden.plant.entity.PlantRepository;
import com.commonplant.garden.plant.exception.PlantErrorCode;
import com.commonplant.garden.place.entity.Place;
import com.commonplant.garden.place.service.PlaceService;
import com.commonplant.garden.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlantServiceImpl implements PlantService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;

    private final PlantRepository plantRepository;
    private final PlaceService placeService;
    private final S3Service s3Service;

    @Override
    @Transactional
    public PlantResponse.DeleteResponse deletePlant(String nanoId, String placeCode, Long plantId) {
        Plant plant = findAccessiblePlant(nanoId, placeCode, plantId);
        Long deletedPlantId = plant.getPlantIdx();

        // 현재 Plant는 imageKey만 보관한다. S3 객체/이미지 메타데이터 삭제는
        // image 도메인 연동 후 별도 정책으로 처리한다.
        plantRepository.delete(plant);

        return PlantResponse.DeleteResponse.of(deletedPlantId);
    }

    @Override
    public PlantResponse.EditInfoResponse getPlantEditInfo(String nanoId, Long plantId) {
        Plant plant = findAccessiblePlant(nanoId, plantId);
        return PlantResponse.EditInfoResponse.of(plant, resolveImageUrl(plant.getImageKey()));
    }

    @Override
    @Transactional
    public PlantResponse.EditInfoResponse updatePlant(
            String nanoId,
            String placeCode,
            Long plantId,
            PlantRequest.UpdateRequest request,
            MultipartFile image
    ) {
        Plant plant = findAccessiblePlant(nanoId, placeCode, plantId);
        validateUpdateRequest(plant, request, image);
        String imageKey = resolveUpdatedImageKey(nanoId, plant.getImageKey(), request, image);

        plant.updateProfile(
                request == null ? null : request.getNickname(),
                request == null ? null : request.getLastWateredDate()
        );
        plant.updateImageKey(imageKey);

        return PlantResponse.EditInfoResponse.of(plant, resolveImageUrl(plant.getImageKey()));
    }

    @Override
    public PlantResponse.DetailResponse getPlant(String nanoId, Long plantId) {
        Plant plant = findAccessiblePlant(nanoId, plantId);
        Place place = placeService.getPlaceById(plant.getPlaceId());

        String memo = findPlantMemo(plant.getPlantIdx());
        return PlantResponse.DetailResponse.of(plant, memo, place.getName(), resolveImageUrl(plant.getImageKey()));
    }

    @Override
    public PlantResponse.PlantListResponse getPlants(String nanoId, int page, int size) {
        List<Long> placeIds = findAccessiblePlaceIds(nanoId);
        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizeSize(size);
        if (placeIds.isEmpty()) {
            return PlantResponse.PlantListResponse.builder()
                    .content(PlantResponse.PlantPageContent.builder()
                            .items(List.of())
                            .totalCount(0)
                            .page(normalizedPage)
                            .size(normalizedSize)
                            .build())
                    .build();
        }

        Page<Plant> plants = plantRepository.findAllByPlaceIdInOrderByPlantIdxDesc(
                placeIds,
                PageRequest.of(normalizedPage, normalizedSize)
        );

        return PlantResponse.PlantListResponse.builder()
                .content(PlantResponse.PlantPageContent.builder()
                        .items(plants.getContent().stream()
                                .map(plant -> PlantResponse.PlantSummary.of(plant, resolveImageUrl(plant.getImageKey())))
                                .toList())
                        .totalCount(plants.getTotalElements())
                        .page(normalizedPage)
                        .size(normalizedSize)
                        .build())
                .build();
    }

    @Override
    public List<PlantResponse.PlantSummary> getPlantsByPlace(String nanoId, String placeCode) {
        Place place = findAccessiblePlace(nanoId, placeCode);

        return plantRepository.findAllByPlaceIdOrderByPlantIdxDesc(place.getPlaceIdx())
                .stream()
                .map(plant -> PlantResponse.PlantSummary.of(plant, resolveImageUrl(plant.getImageKey())))
                .toList();
    }

    @Override
    @Transactional
    public PlantResponse.CreateResponse createPlant(
            String nanoId,
            PlantRequest.CreateRequest request,
            MultipartFile image
    ) {
        Place place = findAccessiblePlace(nanoId, request.getPlaceCode());
        String imageKey = uploadImageIfPresent(nanoId, image);

        Plant plant = Plant.builder()
                .placeId(place.getPlaceIdx())
                .scientificNameKo(request.getScientificNameKo())
                .scientificNameEn(request.getScientificNameEn())
                .nickname(request.getNickname())
                .lastWateredDate(request.getLastWateredDate())
                .imageKey(imageKey)
                .description(request.getDescription())
                .build();

        return PlantResponse.CreateResponse.from(plantRepository.save(plant));
    }

    private Place findAccessiblePlace(String nanoId, String placeCode) {
        Place place = placeService.getPlaceByCode(placeCode);
        placeService.belongUserOnPlace(nanoId, placeCode);
        return place;
    }

    private void validatePlantBelongsToPlace(Plant plant, Long placeId) {
        if (!plant.getPlaceId().equals(placeId)) {
            throw new BusinessException(PlantErrorCode.PLANT_NOT_FOUND);
        }
    }

    private void validateUpdateRequest(Plant plant, PlantRequest.UpdateRequest request, MultipartFile image) {
        if (!StringUtils.hasText(plant.getImageKey()) && request == null && isEmptyFile(image)) {
            throw new BusinessException(PlantErrorCode.NO_FIELDS_TO_UPDATE);
        }

        if (request != null && request.getNickname() != null && !StringUtils.hasText(request.getNickname())) {
            throw new BusinessException(PlantErrorCode.INVALID_NICKNAME);
        }
    }

    private String uploadImageIfPresent(String nanoId, MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return null;
        }
        return s3Service.uploadImage(nanoId, image).getKey();
    }

    private String resolveUpdatedImageKey(
            String nanoId,
            String existingImageKey,
            PlantRequest.UpdateRequest request,
            MultipartFile image
    ) {
        if (hasFile(image)) {
            if (!StringUtils.hasText(existingImageKey)) {
                return s3Service.uploadImage(nanoId, image).getKey();
            }
            return s3Service.updateImage(nanoId, existingImageKey, image).getKey();
        }

        if (!StringUtils.hasText(existingImageKey)) {
            validateAbsentImageKey(request);
            return null;
        }

        if (hasSameImageKey(request, existingImageKey)) {
            return existingImageKey;
        }

        if (hasRequestedImageKey(request)) {
            throw new BusinessException(PlantErrorCode.INVALID_IMAGE_KEY);
        }

        deleteImageIfPresent(nanoId, existingImageKey);
        return null;
    }

    private boolean hasFile(MultipartFile image) {
        return image != null && !image.isEmpty();
    }

    private boolean isEmptyFile(MultipartFile image) {
        return image == null || image.isEmpty();
    }

    private boolean hasRequestedImageKey(PlantRequest.UpdateRequest request) {
        return request != null && StringUtils.hasText(request.getImageKey());
    }

    private boolean hasSameImageKey(PlantRequest.UpdateRequest request, String existingImageKey) {
        return hasRequestedImageKey(request) && request.getImageKey().trim().equals(existingImageKey);
    }

    private void validateAbsentImageKey(PlantRequest.UpdateRequest request) {
        if (hasRequestedImageKey(request)) {
            throw new BusinessException(PlantErrorCode.INVALID_IMAGE_KEY);
        }
    }

    private void deleteImageIfPresent(String nanoId, String imageKey) {
        if (StringUtils.hasText(imageKey)) {
            s3Service.deleteImage(nanoId, imageKey);
        }
    }

    private Plant findAccessiblePlant(String nanoId, String placeCode, Long plantId) {
        Place place = findAccessiblePlace(nanoId, placeCode);
        return findPlantBelongsToPlace(place.getPlaceIdx(), plantId);
    }

    private Plant findAccessiblePlant(String nanoId, Long plantId) {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new BusinessException(PlantErrorCode.PLANT_NOT_FOUND));
        if (!findAccessiblePlaceIds(nanoId).contains(plant.getPlaceId())) {
            throw new BusinessException(PlantErrorCode.PLACE_ACCESS_DENIED);
        }
        return plant;
    }

    private Plant findPlantBelongsToPlace(Long placeId, Long plantId) {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new BusinessException(PlantErrorCode.PLANT_NOT_FOUND));
        validatePlantBelongsToPlace(plant, placeId);
        return plant;
    }

    private List<Long> findAccessiblePlaceIds(String nanoId) {
        return placeService.getPlaceIdsByUser(nanoId);
    }

    private String findPlantMemo(Long plantId) {
        // TODO: memo 도메인 구현 후 plantId 기준 대표/최근 메모를 조회한다.
        return null;
    }

    private String resolveImageUrl(String imageKey) {
        if (!StringUtils.hasText(imageKey)) {
            return null;
        }
        return s3Service.getImageUrl(imageKey);
    }

    private int normalizePage(int page) {
        return Math.max(page, 0);
    }

    private int normalizeSize(int size) {
        if (size < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }
}
