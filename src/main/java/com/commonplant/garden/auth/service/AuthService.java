package com.commonplant.garden.auth.service;

import com.commonplant.garden.auth.dto.AuthRequest;
import com.commonplant.garden.auth.dto.AuthResponse;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {
    Object login(AuthRequest.Login request);
    AuthResponse.RegisterResponse register(AuthRequest.RegisterRequest request, MultipartFile image);
}
