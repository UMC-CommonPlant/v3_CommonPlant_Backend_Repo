package com.commonplant.garden.plant.controller;

import com.commonplant.garden.common.dto.JsonResponse;
import com.commonplant.garden.plant.dto.PlantRequest;
import com.commonplant.garden.plant.dto.PlantResponse;
import com.commonplant.garden.plant.service.PlantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/plants")
@Tag(name = "Plant", description = "식물 생성/조회/수정/삭제 API")
public class PlantController {

    private final PlantService plantService;

    @Operation(summary = "식물 삭제", description = "장소에 속한 식물을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "식물 삭제 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PlantResponse.DeleteJsonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2026-05-12 19:30:00",
                                      "status": 200,
                                      "message": "deletePlant",
                                      "result": {
                                        "plantId": 1
                                      },
                                      "success": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "장소 접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "식물 없음")
    })
    @DeleteMapping("/{plantId}")
    public ResponseEntity<JsonResponse> deletePlant(
            @Parameter(hidden = true) @AuthenticationPrincipal String nanoId,
            @Parameter(description = "식물 ID", example = "1") @PathVariable("plantId") Long plantId,
            @Parameter(description = "식물이 속한 장소 코드", example = "Abc123") @RequestParam("placeCode") String placeCode
    ) {
        PlantResponse.DeleteResponse response = plantService.deletePlant(nanoId, placeCode, plantId);
        return ResponseEntity.ok(new JsonResponse(true, 200, "deletePlant", response));
    }

    @Operation(summary = "식물 수정 정보 조회", description = "식물 수정 화면에 필요한 현재 이미지, 애칭, 마지막 물 준 날짜를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "식물 수정 정보 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PlantResponse.EditInfoJsonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2026-05-12 19:30:00",
                                      "status": 200,
                                      "message": "getPlantEditInfo",
                                      "result": {
                                        "imageKey": "images/user-nano-id/monstera.png",
                                        "imageUrl": "https://bucket.s3.ap-northeast-2.amazonaws.com/images/user-nano-id/monstera.png?X-Amz-Algorithm=...",
                                        "nickname": "거실 몬스테라",
                                        "lastWateredDate": "2026-05-12"
                                      },
                                      "success": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "장소 접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "식물 없음")
    })
    @GetMapping("/{plantId}/edit")
    public ResponseEntity<JsonResponse> getPlantEditInfo(
            @Parameter(hidden = true) @AuthenticationPrincipal String nanoId,
            @Parameter(description = "식물 ID", example = "1") @PathVariable("plantId") Long plantId,
            @Parameter(description = "식물이 속한 장소 코드", example = "Abc123") @RequestParam("placeCode") String placeCode
    ) {
        PlantResponse.EditInfoResponse response = plantService.getPlantEditInfo(nanoId, placeCode, plantId);
        return ResponseEntity.ok(new JsonResponse(true, 200, "getPlantEditInfo", response));
    }

    @Operation(summary = "식물 수정", description = "식물 이미지 파일, 애칭, 마지막 물 준 날짜 중 전달된 값만 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "식물 수정 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PlantResponse.EditInfoJsonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2026-05-12 19:30:00",
                                      "status": 200,
                                      "message": "updatePlant",
                                      "result": {
                                        "imageKey": "images/user-nano-id/monstera-updated.png",
                                        "imageUrl": "https://bucket.s3.ap-northeast-2.amazonaws.com/images/user-nano-id/monstera-updated.png?X-Amz-Algorithm=...",
                                        "nickname": "새 몬스테라",
                                        "lastWateredDate": "2026-05-13"
                                      },
                                      "success": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "수정 요청 값 오류"),
            @ApiResponse(responseCode = "403", description = "장소 접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "식물 없음")
    })
    @PutMapping(value = "/{plantId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JsonResponse> updatePlant(
            @Parameter(hidden = true) @AuthenticationPrincipal String nanoId,
            @Parameter(description = "식물 ID", example = "1") @PathVariable("plantId") Long plantId,
            @Parameter(description = "식물이 속한 장소 코드", example = "Abc123") @RequestParam("placeCode") String placeCode,
            @Parameter(
                    description = "식물 수정 정보(JSON)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PlantRequest.UpdateRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "imageKey": "images/user-nano-id/monstera.png",
                                      "nickname": "새 몬스테라",
                                      "lastWateredDate": "2026-05-13"
                                    }
                                    """))
            )
            @RequestPart(value = "plant", required = false) PlantRequest.UpdateRequest request,
            @Parameter(
                    description = "식물 이미지 파일(선택)",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary"))
            )
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        PlantResponse.EditInfoResponse response = plantService.updatePlant(nanoId, placeCode, plantId, request, image);
        return ResponseEntity.ok(new JsonResponse(true, 200, "updatePlant", response));
    }

    @Operation(summary = "식물 상세 조회", description = "식물 상세 정보와 장소명, 대표 메모 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "식물 상세 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PlantResponse.DetailJsonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2026-05-12 19:30:00",
                                      "status": 200,
                                      "message": "getPlant",
                                      "result": {
                                        "plantId": 1,
                                        "scientificNameKo": "몬스테라",
                                        "scientificNameEn": "Monstera deliciosa",
                                        "registeredAt": "2026-05-12T19:30:00",
                                        "lastWateredDate": "2026-05-12",
                                        "imageUrl": "https://bucket.s3.ap-northeast-2.amazonaws.com/images/user-nano-id/monstera.png?X-Amz-Algorithm=...",
                                        "memo": "새 잎이 올라옴",
                                        "placeName": "거실 정원",
                                        "plantInfo": "햇빛이 잘 드는 거실에서 키우는 몬스테라입니다."
                                      },
                                      "success": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "장소 접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "식물 없음")
    })
    @GetMapping("/{plantId}")
    public ResponseEntity<JsonResponse> getPlant(
            @Parameter(hidden = true) @AuthenticationPrincipal String nanoId,
            @Parameter(description = "식물 ID", example = "1") @PathVariable("plantId") Long plantId,
            @Parameter(description = "식물이 속한 장소 코드", example = "Abc123") @RequestParam("placeCode") String placeCode
    ) {
        PlantResponse.DetailResponse response = plantService.getPlant(nanoId, placeCode, plantId);
        return ResponseEntity.ok(new JsonResponse(true, 200, "getPlant", response));
    }

    @Operation(summary = "내 식물 목록 조회", description = "사용자가 속한 모든 장소의 식물을 페이지 단위로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "식물 목록 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PlantResponse.PlantListJsonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2026-05-12 19:30:00",
                                      "status": 200,
                                      "message": "getPlants",
                                      "result": {
                                        "plants": [
                                          {
                                            "plantId": 1,
                                            "nickname": "거실 몬스테라",
                                            "representativeImageUrl": "https://bucket.s3.ap-northeast-2.amazonaws.com/images/user-nano-id/monstera.png?X-Amz-Algorithm=..."
                                          }
                                        ],
                                        "hasNext": false
                                      },
                                      "success": true
                                    }
                                    """)))
    })
    @GetMapping
    public ResponseEntity<JsonResponse> getPlants(
            @Parameter(hidden = true) @AuthenticationPrincipal String nanoId,
            @Parameter(description = "페이지 번호(0부터 시작)", example = "0")
            @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "페이지 크기(최대 50)", example = "20")
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        PlantResponse.PlantListResponse response = plantService.getPlants(nanoId, page, size);
        return ResponseEntity.ok(new JsonResponse(true, 200, "getPlants", response));
    }

    @Operation(summary = "식물 생성", description = "접근 가능한 장소에 새 식물을 등록합니다. plant는 JSON, image는 선택 업로드입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "식물 생성 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PlantResponse.CreateJsonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2026-05-12 19:30:00",
                                      "status": 201,
                                      "message": "createPlant",
                                      "result": {
                                        "plantId": 1
                                      },
                                      "success": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패"),
            @ApiResponse(responseCode = "403", description = "장소 접근 권한 없음")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JsonResponse> createPlant(
            @Parameter(hidden = true) @AuthenticationPrincipal String nanoId,
            @Parameter(
                    description = "식물 생성 정보(JSON)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PlantRequest.CreateRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "placeCode": "Abc123",
                                      "scientificNameKo": "몬스테라",
                                      "scientificNameEn": "Monstera deliciosa",
                                      "nickname": "거실 몬스테라",
                                      "lastWateredDate": "2026-05-12",
                                      "description": "햇빛이 잘 드는 거실에서 키우는 몬스테라입니다."
                                    }
                                    """))
            )
            @Valid @RequestPart("plant") PlantRequest.CreateRequest request,
            @Parameter(
                    description = "식물 이미지 파일(선택)",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary"))
            )
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        PlantResponse.CreateResponse response = plantService.createPlant(nanoId, request, image);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new JsonResponse(true, 201, "createPlant", response));
    }
}
