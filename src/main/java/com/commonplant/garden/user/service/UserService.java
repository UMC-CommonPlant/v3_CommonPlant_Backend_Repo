package com.commonplant.garden.user.service;

import com.commonplant.garden.user.dto.UserRequest;
import com.commonplant.garden.user.dto.UserResponse;

import java.util.List;

public interface UserService {

    List<UserResponse> getAllUsers();

    UserResponse getUserByIdx(Long userIdx);

    UserResponse getUserByUuid(String uuid);

    UserResponse createUser(UserRequest.CreateRequest request);

    UserResponse updateUser(Long userIdx, UserRequest.UpdateRequest request);

    void deleteUser(Long userIdx);
}
