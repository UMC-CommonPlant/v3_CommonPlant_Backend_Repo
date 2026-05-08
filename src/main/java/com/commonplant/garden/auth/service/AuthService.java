package com.commonplant.garden.auth.service;

import com.commonplant.garden.auth.dto.request.AuthRequest;
import com.commonplant.garden.auth.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse googleLogin(AuthRequest.GoogleLogin request);
}
