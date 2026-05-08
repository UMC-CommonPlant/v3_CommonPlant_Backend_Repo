package com.commonplant.garden.place.controller;

import com.commonplant.garden.common.dto.JsonResponse;
import com.commonplant.garden.place.dto.PlaceDto;
import com.commonplant.garden.place.service.PlaceService;
// import com.commonplant.garden.plant.dto.PlantDto;
import com.commonplant.garden.user.dto.UserResponse;
import com.commonplant.garden.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/place")
public class PlaceController {

    private final PlaceService placeService;
    private final UserService userService;

    @PostMapping(value = "/create",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonResponse> createPlace(
            @AuthenticationPrincipal String nanoId,
            @RequestPart("place") PlaceDto.createPlaceReq req,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        log.info("[API] createPlace");
        UserResponse response = userService.getUserByNanoId(nanoId);

        String placeCode = placeService.create(response.getId(), req, image);
        return ResponseEntity.ok(new JsonResponse(true, 200, "createPlace", placeCode));
    }

    @GetMapping("/{code}")
    public ResponseEntity<JsonResponse> getPlace(
            @AuthenticationPrincipal String nanoId,
            @PathVariable("code") String code) {

        log.info("[API] getPlace");
        UserResponse response = userService.getUserByNanoId(nanoId);

        PlaceDto.getPlaceRes res = placeService.getPlace(response.getId(), code);
        // [TODO]: 장소에 속한 식물의 정보 반환
        // res.setPlantList(plantService.getMyGardenPlantList(code));

        return ResponseEntity.ok(new JsonResponse(true, 200, "getPlace", res));
    }

    @GetMapping("/myGarden")
    public ResponseEntity<JsonResponse> getMyGarden(@AuthenticationPrincipal String nanoId) {
        log.info("[API] getMyGarden");
        UserResponse response = userService.getUserByNanoId(nanoId);

        List<PlaceDto.getPlaceListRes> placeList = placeService.getPlaceList(response.getId());
        // [TODO]: 장소에 속한 식물의 정보 반환
        // List<PlantDto.getPlantListRes> plantList = plantService.getPlantList(response.getId());

        PlaceDto.getMainPage mainPage = new PlaceDto.getMainPage(response.getName(), placeList);
        return ResponseEntity.ok(new JsonResponse(true, 200, "getMyGarden", mainPage));
    }

    @GetMapping("/user")
    public ResponseEntity<JsonResponse> getPlaceBelongUser(@AuthenticationPrincipal String nanoId) {
        log.info("[API] getPlaceBelongUser");
        UserResponse response = userService.getUserByNanoId(nanoId);

        List<PlaceDto.getPlaceBelongUser> belongList =
                placeService.getPlaceBelongUser(response.getId());

        return ResponseEntity.ok(new JsonResponse(true, 200, "getPlaceBelongUser", belongList));
    }

    @PutMapping("/update/{code}")
    public ResponseEntity<JsonResponse> updatePlace(
            @AuthenticationPrincipal String nanoId,
            @PathVariable String code,
            @RequestPart(value = "place") PlaceDto.updatePlaceReq req,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        log.info("[API] updatePlace");
        UserResponse response = userService.getUserByNanoId(nanoId);

        PlaceDto.updatePlaceRes updatedPlace =
                placeService.updatePlace(response.getId(), code, req, image);
        return ResponseEntity.ok(new JsonResponse(true, 200, "updatePlace", updatedPlace));
    }

    @DeleteMapping("/delete/{code}")
    public ResponseEntity<JsonResponse> deletePlace(
            @AuthenticationPrincipal String nanoId,
            @PathVariable String code) {

        log.info("[API] deletePlace");
        UserResponse response = userService.getUserByNanoId(nanoId);

        // [TODO]: Plant, Memo 연동 후 leavePlace로 교체
        // placeService.leavePlace(response.getId(), code);
        placeService.deletePlace(nanoId, code);
        return ResponseEntity.ok(new JsonResponse(true, 200, "deletePlace", null));
    }
}
