package com.commonplant.garden.auth.controller;

import com.commonplant.garden.auth.dto.request.AuthRequest;
import com.commonplant.garden.auth.dto.response.AuthResponse;
import com.commonplant.garden.auth.dto.response.LoginResponse;
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

    @PostMapping("/login")
    public ResponseEntity<JsonResponse> login(@Valid @RequestBody AuthRequest.SocialLogin request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(new JsonResponse(true, 200, "login success", response));
    }

    @PostMapping("/register")
    public ResponseEntity<JsonResponse> register(@Valid @RequestBody AuthRequest.Register request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(new JsonResponse(true, 200, "register success", response));
    }
}
