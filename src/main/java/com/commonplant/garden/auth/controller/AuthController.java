package com.commonplant.garden.auth.controller;

import com.commonplant.garden.auth.dto.AuthRequest;
import com.commonplant.garden.auth.dto.AuthResponse;
import com.commonplant.garden.auth.service.AuthService;
import com.commonplant.garden.common.dto.JsonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerDocs {

    private final AuthService authService;

    @Override
    @PostMapping("/login")
    public ResponseEntity<JsonResponse> login(@Valid @RequestBody AuthRequest.Login request) {
        Object response = authService.login(request);
        return ResponseEntity.ok(new JsonResponse(true, 200, "login", response));
    }

    @Override
    @PostMapping(value = "/register",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonResponse> register(
            @Valid @RequestPart(value = "register") AuthRequest.RegisterRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        AuthResponse.RegisterResponse response = authService.register(request, image);
        return ResponseEntity.ok(new JsonResponse(true, 200, "register", response));
    }
}
