package com.commonplant.garden.place.controller;

import com.commonplant.garden.common.dto.JsonResponse;
import com.commonplant.garden.place.dto.PlaceDto;
import com.commonplant.garden.place.service.PlaceService;
// import com.commonplant.garden.plant.dto.PlantDto;
import com.commonplant.garden.user.dto.UserResponse;
import com.commonplant.garden.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Place", description = "장소(정원) 관련 API")
public class PlaceController {

    private final PlaceService placeService;
    private final UserService userService;

    @Operation(
            summary = "장소 생성",
            description = "새로운 장소(정원)를 생성합니다. place는 JSON, image는 선택 업로드입니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "장소 생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류")
    })
    @PostMapping(value = "/create",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonResponse> createPlace(
            @Parameter(hidden = true) @AuthenticationPrincipal String nanoId,
            @Parameter(description = "장소 생성 정보(JSON)") @RequestPart("place") PlaceDto.createPlaceReq req,
            @Parameter(
                    description = "장소 이미지(선택)",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary"))
            )
            @RequestPart(value = "image", required = false) MultipartFile image) {

        log.info("[API] createPlace");
        UserResponse response = userService.getUserByNanoId(nanoId);

        String placeCode = placeService.create(response.getId(), req, image);
        return ResponseEntity.ok(new JsonResponse(true, 200, "createPlace", placeCode));
    }

    @Operation(summary = "장소 조회", description = "코드로 장소 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "장소 조회 성공"),
            @ApiResponse(responseCode = "404", description = "장소 없음")
    })
    @GetMapping("/{code}")
    public ResponseEntity<JsonResponse> getPlace(
            @Parameter(hidden = true) @AuthenticationPrincipal String nanoId,
            @PathVariable @Parameter(description = "장소 코드") String code) {

        log.info("[API] getPlace");
        UserResponse response = userService.getUserByNanoId(nanoId);

        PlaceDto.getPlaceRes res = placeService.getPlace(response.getId(), code);
        // [TODO]: 장소에 속한 식물의 정보 반환
        // res.setPlantList(plantService.getMyGardenPlantList(code));

        return ResponseEntity.ok(new JsonResponse(true, 200, "getPlace", res));
    }

    @Operation(summary = "내 정원 조회", description = "사용자가 속한 장소 목록과 메인 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정원 조회 성공")
    })
    @GetMapping("/myGarden")
    public ResponseEntity<JsonResponse> getMyGarden(
            @Parameter(hidden = true) @AuthenticationPrincipal String nanoId) {

        log.info("[API] getMyGarden");
        UserResponse response = userService.getUserByNanoId(nanoId);

        List<PlaceDto.getPlaceListRes> placeList = placeService.getPlaceList(response.getId());
        // [TODO]: 장소에 속한 식물의 정보 반환
        // List<PlantDto.getPlantListRes> plantList = plantService.getPlantList(response.getId());

        PlaceDto.getMainPage mainPage = new PlaceDto.getMainPage(response.getName(), placeList);
        return ResponseEntity.ok(new JsonResponse(true, 200, "getMyGarden", mainPage));
    }

    @Operation(summary = "소속 장소 조회", description = "사용자가 속한 장소 리스트를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "소속 장소 조회 성공")
    })
    @GetMapping("/user")
    public ResponseEntity<JsonResponse> getPlaceBelongUser(
            @Parameter(hidden = true) @AuthenticationPrincipal String nanoId) {

        log.info("[API] getPlaceBelongUser");
        UserResponse response = userService.getUserByNanoId(nanoId);

        List<PlaceDto.getPlaceBelongUser> belongList =
                placeService.getPlaceBelongUser(response.getId());

        return ResponseEntity.ok(new JsonResponse(true, 200, "getPlaceBelongUser", belongList));
    }

    @Operation(
            summary = "장소 수정",
            description = "장소 정보를 수정합니다. place는 JSON, image는 선택 업로드입니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "장소 수정 성공"),
            @ApiResponse(responseCode = "404", description = "장소 없음")
    })
    @PutMapping("/update/{code}")
    public ResponseEntity<JsonResponse> updatePlace(
            @Parameter(hidden = true) @AuthenticationPrincipal String nanoId,
            @Parameter(description = "장소 코드") @PathVariable String code,
            @Parameter(description = "장소 수정 정보(JSON)") @RequestPart(value = "place") PlaceDto.updatePlaceReq req,
            @Parameter(
                    description = "장소 이미지(선택)",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary"))
            )
            @RequestPart(value = "image", required = false) MultipartFile image) {

        log.info("[API] updatePlace");
        UserResponse response = userService.getUserByNanoId(nanoId);

        PlaceDto.updatePlaceRes updatedPlace =
                placeService.updatePlace(response.getId(), code, req, image);
        return ResponseEntity.ok(new JsonResponse(true, 200, "updatePlace", updatedPlace));
    }

    @Operation(summary = "장소 삭제", description = "장소를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "장소 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "장소 없음")
    })
    @DeleteMapping("/delete/{code}")
    public ResponseEntity<JsonResponse> deletePlace(
            @Parameter(hidden = true) @AuthenticationPrincipal String nanoId,
            @Parameter(description = "장소 코드") @PathVariable String code) {

        log.info("[API] deletePlace");
        UserResponse response = userService.getUserByNanoId(nanoId);

        // [TODO]: Plant, Memo 연동 후 leavePlace로 교체
        // placeService.leavePlace(response.getId(), code);
        placeService.deletePlace(nanoId, code);
        return ResponseEntity.ok(new JsonResponse(true, 200, "deletePlace", null));
    }
}
