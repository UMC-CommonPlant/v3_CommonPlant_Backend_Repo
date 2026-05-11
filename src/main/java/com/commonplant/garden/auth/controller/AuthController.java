package com.commonplant.garden.auth.controller;

import com.commonplant.garden.auth.dto.request.AuthRequest;
import com.commonplant.garden.auth.dto.response.AuthResponse;
import com.commonplant.garden.auth.service.AuthService;
import com.commonplant.garden.common.dto.JsonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/google")
    public ResponseEntity<JsonResponse> googleLogin(@Valid @RequestBody AuthRequest.GoogleLogin request) {
        AuthResponse response = authService.googleLogin(request);
        return ResponseEntity.ok(new JsonResponse(true, 200, "google login success", response));
    }

    @PostMapping("/kakao")
    public ResponseEntity<JsonResponse> kakaoLogin(@Valid @RequestBody AuthRequest.KakaoLogin request) {
        AuthResponse response = authService.kakaoLogin(request);
        return ResponseEntity.ok(new JsonResponse(true, 200, "kakao login success", response));
    }
}
