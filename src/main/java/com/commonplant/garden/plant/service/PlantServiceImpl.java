package com.commonplant.garden.plant.service;

import com.commonplant.garden.plant.dto.PlantRequest;
import com.commonplant.garden.plant.dto.PlantResponse;
import com.commonplant.garden.plant.entity.Plant;
import com.commonplant.garden.plant.entity.PlantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlantServiceImpl implements PlantService {

    private final PlantRepository plantRepository;

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
        // TODO: place 도메인 구현 후 nanoId가 속해 있는 place list를 반환한다.
        // TODO: 사용자가 place를 선택한 후, 해당 place 접근 권한 검증을 호출한다.
        // TODO: place의 id를 반환한다.
        return placeId;
    }
}
