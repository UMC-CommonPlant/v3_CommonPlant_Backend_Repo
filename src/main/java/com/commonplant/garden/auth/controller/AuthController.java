package com.commonplant.garden.auth.controller;

import com.commonplant.garden.auth.dto.request.AuthRequest;
import com.commonplant.garden.auth.dto.response.AuthResponse;
import com.commonplant.garden.auth.service.AuthService;
import com.commonplant.garden.common.dto.JsonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "소셜 로그인 / 회원가입 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(
            security = @SecurityRequirement(name = ""),
            summary = "소셜 로그인",
            description = """
                    Google / Kakao SDK 토큰으로 로그인합니다.

                    - **기존 유저** (`isNewUser: false`): `accessToken`, `refreshToken` 반환
                    - **신규 유저** (`isNewUser: true`): `signupToken`(10분 유효), `suggestedName`, `suggestedImgUrl` 반환 → `/auth/register` 호출 필요
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "[A005] 지원하지 않는 소셜 제공자 | [A008] 카카오 이메일 제공 동의 필요"),
            @ApiResponse(responseCode = "401", description = "[A001] 유효하지 않은 소셜 토큰 | [A002] 만료된 소셜 토큰"),
            @ApiResponse(responseCode = "409", description = "[A007] 이미 다른 소셜 계정으로 가입된 이메일"),
    })
    @SecurityRequirements
    @PostMapping("/login")
    public ResponseEntity<JsonResponse> login(@Valid @RequestBody AuthRequest.Login request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(new JsonResponse(true, 200, "login success", response));
    }

    @Operation(
            security = @SecurityRequirement(name = ""),
            summary = "회원가입 완료",
            description = """
                    `/auth/login`에서 받은 `signupToken`과 사용자 입력 정보로 회원가입을 완료합니다.

                    - `signupToken` 유효시간: 10분
                    - 성공 시 `accessToken`, `refreshToken` 반환
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "[A005] 지원하지 않는 소셜 제공자"),
            @ApiResponse(responseCode = "401", description = "[A003] 유효하지 않은 signupToken | [A004] 만료된 signupToken (10분 초과)"),
            @ApiResponse(responseCode = "409", description = "[A007] 이미 다른 소셜 계정으로 가입된 이메일 | [A011] 이미 가입된 소셜 계정"),
    })
    @SecurityRequirements
    @PostMapping("/register")
    public ResponseEntity<JsonResponse> register(@Valid @RequestBody AuthRequest.Register request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(new JsonResponse(true, 200, "register success", response));
    }
}
