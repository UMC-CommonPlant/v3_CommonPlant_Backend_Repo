package com.commonplant.garden.plant.service;

import com.commonplant.garden.common.exception.BusinessException;
import com.commonplant.garden.plant.dto.PlantRequest;
import com.commonplant.garden.plant.dto.PlantResponse;
import com.commonplant.garden.plant.entity.Plant;
import com.commonplant.garden.plant.entity.PlantRepository;
import com.commonplant.garden.plant.exception.PlantErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlantServiceImpl implements PlantService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;

    private final PlantRepository plantRepository;

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
            PlantRequest.UpdateRequest request
    ) {
        validateUpdateRequest(request);
        Plant plant = findAccessiblePlant(nanoId, placeId, plantId);

        plant.updateProfile(
                request.getImageKey(),
                request.getNickname(),
                request.getLastWateredDate()
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
    @Transactional
    public PlantResponse.CreateResponse createPlant(String nanoId, PlantRequest.CreateRequest request) {
        Long placeId = resolveAccessiblePlaceId(nanoId, request.getPlaceId());

        Plant plant = Plant.builder()
                .placeId(placeId)
                .scientificNameKo(request.getScientificNameKo())
                .scientificNameEn(request.getScientificNameEn())
                .nickname(request.getNickname())
                .lastWateredDate(request.getLastWateredDate())
                .imageKey(request.getImageKey())
                .description(request.getDescription())
                .build();

        return PlantResponse.CreateResponse.from(plantRepository.save(plant));
    }

    private Long resolveAccessiblePlaceId(String nanoId, Long placeId) {
        validatePlaceAccess(nanoId, placeId);

        // TODO: place 도메인 구현 후 nanoId가 속해 있는 place list를 반환한다.
        // TODO: 사용자가 place를 선택한 후, 해당 place 접근 권한 검증을 호출한다.
        // TODO: place의 id를 반환한다.
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

    private void validateUpdateRequest(PlantRequest.UpdateRequest request) {
        if (request == null || (
                request.getImageKey() == null
                        && request.getNickname() == null
                        && request.getLastWateredDate() == null
        )) {
            throw new BusinessException(PlantErrorCode.NO_FIELDS_TO_UPDATE);
        }

        if (request.getNickname() != null && !StringUtils.hasText(request.getNickname())) {
            throw new BusinessException(PlantErrorCode.INVALID_NICKNAME);
        }

        if (request.getImageKey() != null && !StringUtils.hasText(request.getImageKey())) {
            throw new BusinessException(PlantErrorCode.INVALID_IMAGE_KEY);
        }
    }

    private Plant findAccessiblePlant(String nanoId, Long placeId, Long plantId) {
        validatePlaceAccess(nanoId, placeId);

        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new BusinessException(PlantErrorCode.PLANT_NOT_FOUND));
        validatePlantBelongsToPlace(plant, placeId);
        return plant;
    }

    private List<Long> findAccessiblePlaceIds(String nanoId) {
        // TODO: place 도메인 구현 후 nanoId 기준으로 사용자가 속한 place id 목록을 조회한다.
        // 테스트용: 무조건 placeId 1 반환
        return List.of(1L);
    }

    private String findPlantMemo(Long plantId) {
        // TODO: memo 도메인 구현 후 plantId 기준 대표/최근 메모를 조회한다.
        return null;
    }

    private String findPlaceName(Long placeId) {
        // TODO: place 도메인 구현 후 placeId 기준 장소 이름을 조회한다.
        return null;
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
