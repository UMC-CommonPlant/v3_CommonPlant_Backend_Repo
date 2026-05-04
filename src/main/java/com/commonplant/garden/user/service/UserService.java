package com.commonplant.garden.user.service;

import com.commonplant.garden.user.dto.UserRequest;
import com.commonplant.garden.user.dto.UserResponse;

public interface UserService {

    UserResponse getUserByNanoId(String nanoId);

    UserResponse createUser(UserRequest.CreateRequest request);

    UserResponse updateUser(String nanoId, UserRequest.UpdateRequest request);

    void deleteUser(String username);
}
