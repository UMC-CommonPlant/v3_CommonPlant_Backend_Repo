package com.commonplant.garden.place.controller;

import com.commonplant.garden.common.dto.JsonResponse;
import com.commonplant.garden.place.dto.PlaceDto;
import com.commonplant.garden.place.facade.PlaceFacade;
import com.commonplant.garden.place.service.PlaceService;
// import com.commonplant.garden.plant.dto.PlantDto;
import com.commonplant.garden.user.dto.UserResponse;
import com.commonplant.garden.user.service.UserService;
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
    private final PlaceFacade placeFacade;
    private final UserService userService;

    @Operation(
            summary = "장소 생성",
            description = "새로운 장소(정원)를 생성합니다. place는 JSON, image는 선택 업로드입니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "장소 생성 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2026-05-21 19:30:00",
                                      "status": 200,
                                      "message": "createPlace",
                                      "result": "ABCabc",
                                      "success": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패 - 장소 이름 10자 초과",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                {
                                  "timeStamp": "2026-05-21 19:30:00",
                                  "status": 400,
                                  "message": "P107",
                                  "result": null,
                                  "success": false
                                }
                                """))),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패 - 주소 누락/공백",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                {
                                  "timeStamp": "2026-05-21 19:30:00",
                                  "status": 400,
                                  "message": "P108",
                                  "result": null,
                                  "success": false
                                }
                                """))),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패 - 이름 누락/공백",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                {
                                  "timeStamp": "2026-05-21 19:30:00",
                                  "status": 400,
                                  "message": "P109",
                                  "result": null,
                                  "success": false
                                }
                                """)))
    })
    @PostMapping(value = "/create",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonResponse> createPlace(
            @Parameter(hidden = true) @AuthenticationPrincipal String nanoId,
            @Parameter(
                    description = "장소 생성 정보(JSON)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PlaceDto.createPlaceReq.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "name": "거실 정원",
                                      "address": "서울특별시 ..."
                                    }
                                    """)
                    )
            )
            @Valid @RequestPart("place") PlaceDto.createPlaceReq req,
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
            @ApiResponse(responseCode = "200", description = "장소 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2026-05-21 19:30:00",
                                      "status": 200,
                                      "message": "getPlace",
                                      "result": {
                                        "name": "거실 정원",
                                        "code": "ABCabc",
                                        "address": "서울특별시 ...",
                                        "imgUrl": "https://bucket.s3.ap-northeast-2.amazonaws.com/images/user-nano-id/garden.png?X-Amz-Algorithm=...",
                                        "userList": [
                                          {
                                            "name": "홍길동",
                                            "image": "https://bucket.s3.ap-northeast-2.amazonaws.com/images/user-nano-id/user.png"
                                          },
                                          {
                                            "name": "김철수",
                                            "image": "https://bucket.s3.ap-northeast-2.amazonaws.com/images/user-nano-id/user2.png"
                                          }
                                        ],
                                        "plantList": [
                                          {
                                            "plantId": 1,
                                            "scientificNameKo": "몬스테라",
                                            "scientificNameEn": "Monstera deliciosa",
                                            "registeredAt": "2026-06-30T16:55:51.387461",
                                            "lastWateredDate": "2026-05-12",
                                            "imageUrl": "https://bucket.s3.ap-northeast-2.amazonaws.com/images/user-nano-id/monstera.png?X-Amz-Algorithm=...",
                                            "memo": "새 잎이 올라옴",
                                            "placeName": "거실 정원",
                                            "plantInfo": "햇빛이 잘 드는 거실에서 키우는 몬스테라입니다."
                                          },
                                          {
                                            "plantId": 2,
                                            "scientificNameKo": "고무나무",
                                            "scientificNameEn": "Ficus elastica",
                                            "registeredAt": "2026-06-29T10:30:00",
                                            "lastWateredDate": "2026-06-30",
                                            "imageUrl": "https://bucket.s3.ap-northeast-2.amazonaws.com/images/user-nano-id/ficus.png?X-Amz-Algorithm=...",
                                            "memo": null,
                                            "placeName": "거실 정원",
                                            "plantInfo": null
                                          }
                                        ],
                                        "owner": true
                                      },
                                      "success": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "장소 접근 권한 없음",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2026-05-21 19:30:00",
                                      "status": 403,
                                      "message": "P103",
                                      "result": null,
                                      "success": false
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "장소 없음",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2026-05-21 19:30:00",
                                      "status": 404,
                                      "message": "P101",
                                      "result": null,
                                      "success": false
                                    }
                                    """)))
    })
    @GetMapping("/{code}")
    public ResponseEntity<JsonResponse> getPlace(
            @Parameter(hidden = true) @AuthenticationPrincipal String nanoId,
            @PathVariable @Parameter(description = "장소 코드", example = "ABCabc") String code) {

        log.info("[API] getPlace");
        UserResponse response = userService.getUserByNanoId(nanoId);

        PlaceDto.getPlaceRes res = placeFacade.getPlace(response.getId(), code);
        return ResponseEntity.ok(new JsonResponse(true, 200, "getPlace", res));
    }

    @Operation(summary = "내 정원 조회", description = "사용자가 속한 장소 목록과 메인 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정원 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2026-05-21 19:30:00",
                                      "status": 200,
                                      "message": "getMyGarden",
                                      "result": {
                                        "name": "홍길동",
                                        "placeList": [
                                          {
                                            "image": "https://.../garden.png",
                                            "code": "ABCabc",
                                            "name": "거실 정원",
                                            "member": "2",
                                            "plant": "0"
                                          }
                                        ]
                                      },
                                      "success": true
                                    }
                                    """)))
    })
    @GetMapping("/myGarden")
    public ResponseEntity<JsonResponse> getMyGarden(
            @Parameter(hidden = true) @AuthenticationPrincipal String nanoId) {

        log.info("[API] getMyGarden");
        UserResponse response = userService.getUserByNanoId(nanoId);

        List<PlaceDto.getPlaceListRes> placeList = placeService.getPlaceList(response.getId());
        PlaceDto.getMainPage mainPage = new PlaceDto.getMainPage(response.getName(), placeList);
        return ResponseEntity.ok(new JsonResponse(true, 200, "getMyGarden", mainPage));
    }

    @Operation(summary = "소속 장소 조회", description = "사용자가 속한 장소 리스트를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "소속 장소 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2026-05-21 19:30:00",
                                      "status": 200,
                                      "message": "getPlaceBelongUser",
                                      "result": [
                                        {
                                          "code": "ABCabc",
                                          "name": "거실 정원",
                                          "imgUrl": "https://.../garden.png"
                                        }
                                      ],
                                      "success": true
                                    }
                                    """)))
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
            @ApiResponse(responseCode = "200", description = "장소 수정 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                {
                                  "timeStamp": "2026-05-21 19:30:00",
                                  "status": 200,
                                  "message": "updatePlace",
                                  "result": {
                                    "code": "ABCabc",
                                    "name": "새로운 정원",
                                    "address": "경기도 새로운 주소",
                                    "imgUrl": "https://.../garden-updated.png"
                                  },
                                  "success": true
                                }
                                """))),

            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패 - 장소 이름 누락",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                {
                                  "timeStamp": "2026-05-21 19:30:00",
                                  "status": 400,
                                  "message": "P109",
                                  "result": null,
                                  "success": false
                                }
                                """))),

            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패 - 장소 이름 10자 초과",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                {
                                  "timeStamp": "2026-05-21 19:30:00",
                                  "status": 400,
                                  "message": "P107",
                                  "result": null,
                                  "success": false
                                }
                                """))),

            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패 - 주소 누락/공백",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                {
                                  "timeStamp": "2026-05-21 19:30:00",
                                  "status": 400,
                                  "message": "P108",
                                  "result": null,
                                  "success": false
                                }
                                """))),

            @ApiResponse(responseCode = "400", description = "장소 이미지 키 오류",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                {
                                  "timeStamp": "2026-05-21 19:30:00",
                                  "status": 400,
                                  "message": "P106",
                                  "result": null,
                                  "success": false
                                }
                                """))),

            @ApiResponse(responseCode = "403", description = "장소 접근 권한 없음",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                {
                                  "timeStamp": "2026-05-21 19:30:00",
                                  "status": 403,
                                  "message": "P103",
                                  "result": null,
                                  "success": false
                                }
                                """))),

            @ApiResponse(responseCode = "404", description = "장소 없음",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                {
                                  "timeStamp": "2026-05-21 19:30:00",
                                  "status": 404,
                                  "message": "P101",
                                  "result": null,
                                  "success": false
                                }
                                """)))
    })
    @PutMapping(value = "/update/{code}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JsonResponse> updatePlace(
            @Parameter(hidden = true) @AuthenticationPrincipal String nanoId,
            @Parameter(description = "장소 코드", example = "ABCabc") @PathVariable String code,
            @Parameter(
                    description = "장소 수정 정보(JSON)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PlaceDto.updatePlaceReq.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "imageKey": "images/user-nano-id/garden.png",
                                      "name": "새로운 정원",
                                      "address": "경기도 새로운 주소"
                                    }
                                    """))
            )
            @Valid @RequestPart(value = "place") PlaceDto.updatePlaceReq req,
            @Parameter(
                    description = "장소 이미지 파일(선택)",
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
            @ApiResponse(responseCode = "200", description = "장소 삭제 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2026-05-21 19:30:00",
                                      "status": 200,
                                      "message": "deletePlace",
                                      "result": null,
                                      "success": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "장소 접근 권한 없음",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2026-05-21 19:30:00",
                                      "status": 403,
                                      "message": "P103",
                                      "result": null,
                                      "success": false
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "장소 없음",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "timeStamp": "2026-05-21 19:30:00",
                                      "status": 404,
                                      "message": "P101",
                                      "result": null,
                                      "success": false
                                    }
                                    """)))
    })
    @DeleteMapping("/delete/{code}")
    public ResponseEntity<JsonResponse> deletePlace(
            @Parameter(hidden = true) @AuthenticationPrincipal String nanoId,
            @Parameter(description = "장소 코드", example = "ABCabc") @PathVariable String code) {

        log.info("[API] deletePlace");
        UserResponse response = userService.getUserByNanoId(nanoId);

        placeService.deletePlace(nanoId, code);
        return ResponseEntity.ok(new JsonResponse(true, 200, "deletePlace", null));
    }
}
