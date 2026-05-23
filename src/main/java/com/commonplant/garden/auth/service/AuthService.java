package com.commonplant.garden.auth.service;

import com.commonplant.garden.auth.dto.AuthRequest;
import com.commonplant.garden.auth.dto.AuthResponse;

public interface AuthService {
    Object login(AuthRequest.Login request);
    AuthResponse.Register register(AuthRequest.Register request);
}
