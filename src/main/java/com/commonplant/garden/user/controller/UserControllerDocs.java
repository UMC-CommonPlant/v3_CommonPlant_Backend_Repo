package com.commonplant.garden.user.controller;

import com.commonplant.garden.common.dto.JsonResponse;
import com.commonplant.garden.user.dto.UserRequest;
import com.commonplant.garden.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "User", description = "사용자 정보 API")
@SecurityRequirement(name = "bearerAuth")
public interface UserControllerDocs {

    /* GET /users/{keyword} - 사용자 이름 검색 */
    @Operation(summary = "사용자 이름 검색", description = "키워드로 사용자를 검색합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.UserListJsonResponse.class),
                            examples = @ExampleObject(name = "success", summary = "사용자 검색 성공 응답", value = """
                                    {
                                      "timeStamp": "2026-05-12 19:30:00",
                                      "status": 200,
                                      "message": "searchUserByName",
                                      "result": [
                                        {
                                          "name": "홍길동",
                                          "id": "NpGFNGZ3mZGN",
                                          "email": "user@example.com",
                                          "provider": "GOOGLE",
                                          "imgUrl": "https://bucket.s3.ap-northeast-2.amazonaws.com/images/profile.png",
                                          "introduction": "몬스테라를 키우는 식집사입니다."
                                        }
                                      ],
                                      "success": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "[U002] 지원하지 않는 키워드 형식"),
            @ApiResponse(responseCode = "401", description = "[A003] 유효하지 않은 JWT | [A004] 만료된 JWT"),
            @ApiResponse(responseCode = "404", description = "[U101] 사용자를 찾을 수 없음"),
    })
    ResponseEntity<JsonResponse> searchUserByName(
            @Parameter(description = "검색 키워드 (사용자 이름)", example = "홍길동") String keyword
    );

    // ──────────────────────────────────────────────────────────────────────────
    /* GET /users - 내 정보 조회 */
    @Operation(summary = "내 정보 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.UserJsonResponse.class),
                            examples = @ExampleObject(name = "success", summary = "내 정보 조회 성공 응답", value = """
                                    {
                                      "timeStamp": "2026-05-12 19:30:00",
                                      "status": 200,
                                      "message": "getUserByNanoId",
                                      "result": {
                                        "name": "홍길동",
                                        "id": "NpGFNGZ3mZGN",
                                        "email": "user@example.com",
                                        "provider": "GOOGLE",
                                        "imgUrl": "https://bucket.s3.ap-northeast-2.amazonaws.com/images/profile.png",
                                        "introduction": "몬스테라를 키우는 식집사입니다."
                                      },
                                      "success": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "[A003] 유효하지 않은 JWT | [A004] 만료된 JWT"),
            @ApiResponse(responseCode = "404", description = "[U101] 사용자를 찾을 수 없음"),
    })
    ResponseEntity<JsonResponse> getUserByNanoId(
            @Parameter(hidden = true) String nanoId
    );

    // ──────────────────────────────────────────────────────────────────────────
    /* PUT /users - 내 정보 수정 */
    @Operation(summary = "내 정보 수정", description = "전달한 필드만 수정됩니다. 수정하지 않을 필드는 생략하세요.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.UserJsonResponse.class),
                            examples = @ExampleObject(name = "success", summary = "내 정보 수정 성공 응답", value = """
                                    {
                                      "timeStamp": "2026-05-12 19:30:00",
                                      "status": 200,
                                      "message": "updateUser",
                                      "result": {
                                        "name": "홍길동",
                                        "id": "NpGFNGZ3mZGN",
                                        "email": "user@example.com",
                                        "provider": "GOOGLE",
                                        "imgUrl": "https://bucket.s3.ap-northeast-2.amazonaws.com/images/profile-updated.png",
                                        "introduction": "수정된 소개입니다."
                                      },
                                      "success": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "[A003] 유효하지 않은 JWT | [A004] 만료된 JWT"),
            @ApiResponse(responseCode = "404", description = "[U101] 사용자를 찾을 수 없음"),
    })
    @RequestBody(
            description = "사용자 정보 수정 multipart 요청",
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(implementation = UserRequest.UpdateMultipartRequest.class),
                    encoding = {
                            @Encoding(name = "user",  contentType = MediaType.APPLICATION_JSON_VALUE),
                            @Encoding(name = "image", contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    })
    )
    ResponseEntity<JsonResponse> updateUser(
            @Parameter(hidden = true) String nanoId,
            @Parameter(
                    description = "수정할 사용자 정보(JSON)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserRequest.UpdateRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "name": "홍길동",
                                      "introduction": "수정된 소개입니다."
                                    }
                                    """))
            ) UserRequest.UpdateRequest request,
            @Parameter(
                    description = "사용자 프로필 이미지(선택)",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary"))
            ) MultipartFile image
    );

    // ──────────────────────────────────────────────────────────────────────────
    /* DELETE /users - 회원 탈퇴 */
    @Operation(summary = "회원 탈퇴", description = "Soft delete 처리됩니다. (status: DELETED)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "탈퇴 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.DeleteJsonResponse.class),
                            examples = @ExampleObject(name = "success", summary = "회원 탈퇴 성공 응답", value = """
                                    {
                                      "timeStamp": "2026-05-12 19:30:00",
                                      "status": 200,
                                      "message": "deleteUser",
                                      "result": null,
                                      "success": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "[A003] 유효하지 않은 JWT | [A004] 만료된 JWT"),
            @ApiResponse(responseCode = "404", description = "[U101] 사용자를 찾을 수 없음"),
    })
    ResponseEntity<JsonResponse> deleteUser(
            @Parameter(hidden = true) String nanoId
    );
}
