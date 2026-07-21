package com.commonplant.garden.place.service;

import com.commonplant.garden.place.dto.PlaceDto;
import com.commonplant.garden.place.entity.Place;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PlaceService {

    String create(String nanoId, PlaceDto.createPlaceReq req, MultipartFile image);

    List<PlaceDto.getPlaceListRes> getPlaceList(String nanoId);

    PlaceDto.getPlaceGridRes getPlaceGrid(String nanoId, String code);

    List<PlaceDto.getPlaceBelongUser> getPlaceBelongUser(String nanoId);

    PlaceDto.updatePlaceRes updatePlace(String nanoId, String code, PlaceDto.updatePlaceReq req, MultipartFile image);

    void deletePlace(String nanoId, String code);

    // helper
    void belongUserOnPlace(String nanoId, String code);

    List<Long> getPlaceIdsByUser(String nanoId);

    String getPlaceNameById(Long placeId);

    String getPlaceAddressById(Long placeId);

    Place getPlaceById(Long placeId);

    Place getPlaceByCode(String code);
}
