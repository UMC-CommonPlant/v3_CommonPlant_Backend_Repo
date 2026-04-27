package com.commonplant.garden.user.service;

import com.commonplant.garden.common.exception.BusinessException;
import com.commonplant.garden.user.dto.UserRequest;
import com.commonplant.garden.user.dto.UserResponse;
import com.commonplant.garden.user.entity.User;
import com.commonplant.garden.user.entity.UserRepository;
import com.commonplant.garden.user.enums.UserStatus;
import com.commonplant.garden.user.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAllByStatus(UserStatus.ACTIVE)
                .stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse getUserByIdx(Long userIdx) {
        return UserResponse.from(findActiveUserByIdx(userIdx));
    }

    @Override
    public UserResponse getUserByUuid(String uuid) {
        User user = userRepository.findByUuidAndStatus(uuid, UserStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }

    @Override
    @Transactional
    public UserResponse createUser(UserRequest.CreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(UserErrorCode.DUPLICATE_EMAIL);
        }
        if (userRepository.existsByName(request.getName())) {
            throw new BusinessException(UserErrorCode.DUPLICATE_NICKNAME);
        }
        User user = User.builder()
                .uuid(UUID.randomUUID().toString())
                .name(request.getName())
                .email(request.getEmail())
                .provider(request.getProvider())
                .providerId(request.getProviderId())
                .imgUrl(request.getImgUrl())
                .build();
        return UserResponse.from(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long userIdx, UserRequest.UpdateRequest request) {
        User user = findActiveUserByIdx(userIdx);
        user.updateProfile(request.getName(), request.getIntroduction(), request.getImgUrl());
        return UserResponse.from(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userIdx) {
        findActiveUserByIdx(userIdx).deactivate();
    }

    // ── private helper ──────────────────────────────────────────────────────

    private User findActiveUserByIdx(Long userIdx) {
        return userRepository.findByUserIdxAndStatus(userIdx, UserStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }
}