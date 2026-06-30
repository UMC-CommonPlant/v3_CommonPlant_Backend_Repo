package com.commonplant.garden.auth.controller;

import com.commonplant.garden.auth.dto.AuthRequest;
import com.commonplant.garden.auth.dto.AuthResponse;
import com.commonplant.garden.common.dto.JsonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Auth", description = "소셜 로그인 / 회원가입 API")
public interface AuthControllerDocs {

    /* POST /auth/login - 로그인 */
    @Operation(
            summary = "로그인",
            description = """
                    Google / Kakao SDK 토큰으로 로그인합니다.

                    - **기존 유저** (`isNewUser: false`): `accessToken`, `refreshToken` 반환
                    - **신규 유저** (`isNewUser: true`): `signupToken`(10분 유효), `suggestedName`, `suggestedImgUrl` 반환 → `/auth/register` 호출 필요
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(oneOf = {
                                    AuthResponse.LoginSuccessJsonResponse.class,
                                    AuthResponse.LoginNewUserJsonResponse.class
                            }),
                            examples = {
                                    @ExampleObject(name = "기존 유저", summary = "기존 유저 로그인 응답", value = """
                                            {
                                              "timeStamp": "2026-05-12 19:30:00",
                                              "status": 200,
                                              "message": "login",
                                              "result": {
                                                "isNewUser": false,
                                                "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                                                "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
                                              },
                                              "success": true
                                            }
                                            """),
                                    @ExampleObject(name = "신규 유저", summary = "신규 유저 로그인 응답 (회원가입 필요)", value = """
                                            {
                                              "timeStamp": "2026-05-12 19:30:00",
                                              "status": 200,
                                              "message": "login",
                                              "result": {
                                                "isNewUser": true,
                                                "signupToken": "eyJhbGciOiJIUzI1NiJ9...",
                                                "suggestedName": "홍길동",
                                                "suggestedImgUrl": "https://lh3.googleusercontent.com/..."
                                              },
                                              "success": true
                                            }
                                            """)
                            })),
            @ApiResponse(responseCode = "400", description = "[A005] 지원하지 않는 소셜 제공자 | [A008] 카카오 이메일 제공 동의 필요"),
            @ApiResponse(responseCode = "401", description = "[A001] 유효하지 않은 소셜 토큰 | [A002] 만료된 소셜 토큰"),
            @ApiResponse(responseCode = "409", description = "[A007] 이미 다른 소셜 계정으로 가입된 이메일"),
    })
    @SecurityRequirements
    ResponseEntity<JsonResponse> login(@Valid AuthRequest.Login request);

    // ──────────────────────────────────────────────────────────────────────────
    /* POST /auth/register - 회원가입 */
    @Operation(
            summary = "회원가입 완료",
            description = """
                    `/auth/login`에서 받은 `signupToken`과 사용자 입력 정보로 회원가입을 완료합니다.

                    - `signupToken` 유효시간: 10분
                    - 성공 시 `accessToken`, `refreshToken` 반환
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthResponse.RegisterJsonResponse.class),
                            examples = @ExampleObject(name = "success", summary = "회원가입 성공 응답", value = """
                                    {
                                      "timeStamp": "2026-05-12 19:30:00",
                                      "status": 200,
                                      "message": "register",
                                      "result": {
                                        "isNewUser": false,
                                        "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                                        "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
                                      },
                                      "success": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "[A005] 지원하지 않는 소셜 제공자"),
            @ApiResponse(responseCode = "401", description = "[A006] 유효하지 않은 signupToken | [A004] 만료된 Token (10분 초과)"),
            @ApiResponse(responseCode = "409", description = "[A007] 이미 다른 소셜 계정으로 가입된 이메일 | [A011] 이미 가입된 소셜 계정"),
    })
    @RequestBody(
            description = "회원가입 multipart 요청",
            required = true,
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(implementation = AuthRequest.RegisterMultipartRequest.class),
                    encoding = {
                            @Encoding(name = "register", contentType = MediaType.APPLICATION_JSON_VALUE),
                            @Encoding(name = "image",    contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    })
    )
    @SecurityRequirements
    ResponseEntity<JsonResponse> register(
            @Parameter(
                    description = "회원가입 정보(JSON)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthRequest.RegisterRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "signupToken": "eyJhbGciOiJIUzI1NiJ9...",
                                      "name": "홍길동",
                                      "introduction": "몬스테라를 키우는 식집사입니다."
                                    }
                                    """))
            ) @Valid AuthRequest.RegisterRequest request,
            @Parameter(
                    description = "사용자 프로필 이미지(선택)",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary"))
            ) MultipartFile image
    );
}
