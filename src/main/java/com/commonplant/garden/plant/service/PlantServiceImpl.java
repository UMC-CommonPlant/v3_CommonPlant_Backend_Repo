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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
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
    public PlantResponse.DeleteResponse deletePlant(String nanoId, Long placeId, Long plantId) {
        Plant plant = findAccessiblePlant(nanoId, placeId, plantId);
        Long deletedPlantId = plant.getPlantIdx();

        // 현재 Plant는 imageKey만 보관한다. S3 객체/이미지 메타데이터 삭제는
        // image 도메인 연동 후 별도 정책으로 처리한다.
        plantRepository.delete(plant);

        return PlantResponse.DeleteResponse.of(deletedPlantId);
    }

    @Override
    public PlantResponse.EditInfoResponse getPlantEditInfo(String nanoId, Long placeId, Long plantId) {
        Plant plant = findAccessiblePlant(nanoId, placeId, plantId);
        return PlantResponse.EditInfoResponse.from(plant);
    }

    @Override
    @Transactional
    public PlantResponse.EditInfoResponse updatePlant(
            String nanoId,
            Long placeId,
            Long plantId,
            PlantRequest.UpdateRequest request,
            MultipartFile image
    ) {
        validateUpdateRequest(request, image);
        Plant plant = findAccessiblePlant(nanoId, placeId, plantId);
        String imageKey = updateImageIfPresent(nanoId, plant.getImageKey(), image);

        plant.updateProfile(
                imageKey,
                request == null ? null : request.getNickname(),
                request == null ? null : request.getLastWateredDate()
        );

        return PlantResponse.EditInfoResponse.from(plant);
    }

    @Override
    public PlantResponse.DetailResponse getPlant(String nanoId, Long placeId, Long plantId) {
        Plant plant = findAccessiblePlant(nanoId, placeId, plantId);

        String memo = findPlantMemo(plant.getPlantIdx());
        String placeName = findPlaceName(placeId);
        return PlantResponse.DetailResponse.of(plant, memo, placeName);
    }

    @Override
    public PlantResponse.PlantListResponse getPlants(String nanoId, int page, int size) {
        List<Long> placeIds = findAccessiblePlaceIds(nanoId);
        if (placeIds.isEmpty()) {
            return PlantResponse.PlantListResponse.builder()
                    .plants(List.of())
                    .hasNext(false)
                    .build();
        }

        Slice<Plant> plants = plantRepository.findAllByPlaceIdInOrderByPlantIdxDesc(
                placeIds,
                PageRequest.of(normalizePage(page), normalizeSize(size))
        );

        return PlantResponse.PlantListResponse.builder()
                .plants(plants.getContent().stream()
                        .map(PlantResponse.PlantSummary::from)
                        .toList())
                .hasNext(plants.hasNext())
                .build();
    }

    @Override
    public List<PlantResponse.PlantSummary> getPlantsByPlace(String nanoId, String placeCode) {
        Place place = placeService.getPlaceByCode(placeCode);
        validatePlaceAccess(nanoId, place.getPlaceIdx());

        return plantRepository.findAllByPlaceIdOrderByPlantIdxDesc(place.getPlaceIdx())
                .stream()
                .map(PlantResponse.PlantSummary::from)
                .toList();
    }

    @Override
    @Transactional
    public PlantResponse.CreateResponse createPlant(
            String nanoId,
            PlantRequest.CreateRequest request,
            MultipartFile image
    ) {
        Long placeId = resolveAccessiblePlaceId(nanoId, request.getPlaceId());
        String imageKey = uploadImageIfPresent(nanoId, image);

        Plant plant = Plant.builder()
                .placeId(placeId)
                .scientificNameKo(request.getScientificNameKo())
                .scientificNameEn(request.getScientificNameEn())
                .nickname(request.getNickname())
                .lastWateredDate(request.getLastWateredDate())
                .imageKey(imageKey)
                .description(request.getDescription())
                .build();

        return PlantResponse.CreateResponse.from(plantRepository.save(plant));
    }

    private Long resolveAccessiblePlaceId(String nanoId, Long placeId) {
        validatePlaceAccess(nanoId, placeId);
        return placeId;
    }

    private void validatePlaceAccess(String nanoId, Long placeId) {
        if (!findAccessiblePlaceIds(nanoId).contains(placeId)) {
            throw new BusinessException(PlantErrorCode.PLACE_ACCESS_DENIED);
        }
    }

    private void validatePlantBelongsToPlace(Plant plant, Long placeId) {
        if (!plant.getPlaceId().equals(placeId)) {
            throw new BusinessException(PlantErrorCode.PLANT_NOT_FOUND);
        }
    }

    private void validateUpdateRequest(PlantRequest.UpdateRequest request, MultipartFile image) {
        if ((request == null || (
                request.getNickname() == null
                        && request.getLastWateredDate() == null
        )) && (image == null || image.isEmpty())) {
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

    private String updateImageIfPresent(String nanoId, String existingImageKey, MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return null;
        }
        if (!StringUtils.hasText(existingImageKey)) {
            return s3Service.uploadImage(nanoId, image).getKey();
        }
        return s3Service.updateImage(nanoId, existingImageKey, image).getKey();
    }

    private Plant findAccessiblePlant(String nanoId, Long placeId, Long plantId) {
        validatePlaceAccess(nanoId, placeId);

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

    private String findPlaceName(Long placeId) {
        return placeService.getPlaceNameById(placeId);
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
