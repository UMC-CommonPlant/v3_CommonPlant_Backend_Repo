package com.commonplant.garden.user.service;

import com.commonplant.garden.user.dto.UserRequest;
import com.commonplant.garden.user.dto.UserResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    UserResponse getUserByNanoId(String nanoId);

    List<UserResponse> searchUserByName(String keyword);

    UserResponse updateUser(String nanoId, UserRequest.UpdateRequest request, MultipartFile image);

    void deleteUser(String username);

    // ── helper ──────────────────────────────────────────────────────
    UserResponse searchActiveUserByNanoId(String nanoId);

    List<UserResponse> searchActiveUsersByUsername(String keyword);

}
