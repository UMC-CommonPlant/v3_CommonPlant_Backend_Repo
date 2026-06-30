package com.commonplant.garden.place.facade;

import com.commonplant.garden.belong.entity.BelongRepository;
import com.commonplant.garden.common.exception.BusinessException;
import com.commonplant.garden.place.dto.PlaceDto;
import com.commonplant.garden.place.entity.Place;
import com.commonplant.garden.place.exception.PlaceErrorCode;
import com.commonplant.garden.place.service.PlaceService;
import com.commonplant.garden.plant.dto.PlantResponse;
import com.commonplant.garden.plant.service.PlantService;
import com.commonplant.garden.s3.service.S3Service;
import com.commonplant.garden.user.entity.User;
import com.commonplant.garden.user.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceFacade {

    private final PlaceService placeService;
    private final PlantService plantService;
    private final UserServiceImpl userService;
    private final BelongRepository belongRepository;
    private final S3Service s3Service;

    public PlaceDto.getPlaceRes getPlace(String nanoId, String code) {
        User user = userService.findActiveUserByNanoId(nanoId);

        placeService.belongUserOnPlace(user.getNanoId(), code);
        Place place = placeService.getPlaceByCode(code);

        List<PlaceDto.getPlaceResUser> userList =
                belongRepository.getUserListByPlaceCode(code)
                        .orElseThrow(() -> new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND))
                        .stream()
                        .map(u -> new PlaceDto.getPlaceResUser(u.getName(), u.getImgUrl()))
                        .toList();

        List<Long> plantIds = plantService.getPlantIdsByPlace(nanoId, code);

        List<PlantResponse.DetailResponse> plantList = plantIds.stream()
                .map(plantId -> plantService.getPlant(nanoId, plantId))
                .toList();

        boolean isOwner = place.getOwner().getUserIdx().equals(user.getUserIdx());

        return PlaceDto.getPlaceRes.builder()
                .name(place.getName())
                .address(place.getAddress())
                .code(place.getCode())
                .isOwner(isOwner)
                .imgUrl(resolveImageUrl(place.getImgUrl()))
                .userList(userList)
                .plantList(plantList)
                .build();
    }

    private String resolveImageUrl(String imageKey) {
        if (imageKey == null || imageKey.isBlank()) return null;
        return s3Service.getImageUrl(imageKey);
    }
}
