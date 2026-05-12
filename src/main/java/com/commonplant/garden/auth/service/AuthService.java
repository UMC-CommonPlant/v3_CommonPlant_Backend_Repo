package com.commonplant.garden.auth.service;

import com.commonplant.garden.auth.dto.request.AuthRequest;
import com.commonplant.garden.auth.dto.response.AuthResponse;
import com.commonplant.garden.auth.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse login(AuthRequest.SocialLogin request);
    AuthResponse register(AuthRequest.Register request);
}
