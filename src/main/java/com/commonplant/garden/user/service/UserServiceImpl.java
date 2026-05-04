package com.commonplant.garden.user.service;

import com.commonplant.garden.common.exception.BusinessException;
import com.commonplant.garden.common.util.IdUtil;
import com.commonplant.garden.user.dto.UserRequest;
import com.commonplant.garden.user.dto.UserResponse;
import com.commonplant.garden.user.entity.User;
import com.commonplant.garden.user.entity.UserRepository;
import com.commonplant.garden.user.enums.UserStatus;
import com.commonplant.garden.user.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponse getUserByNanoId(String nanoId) {
        return UserResponse.from(findActiveUserByNanoId(nanoId));
    }

    @Override
    @Transactional
    public UserResponse createUser(UserRequest.CreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(UserErrorCode.DUPLICATE_EMAIL);
        }
        if (userRepository.existsByProviderAndProviderId(request.getProvider(), request.getProviderId())) {
            throw new BusinessException(UserErrorCode.DUPLICATE_PROVIDER);
        }
        User user = User.builder()
                .nanoId(IdUtil.generateNanoId())
                .name(request.getName())
                .email(request.getEmail())
                .introduction(request.getIntroduction())
                .provider(request.getProvider())
                .providerId(request.getProviderId())
                .imgUrl(request.getImgUrl())
                .build();
        return UserResponse.from(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateUser(String nanoId, UserRequest.UpdateRequest request) {
        User user = findActiveUserByNanoId(nanoId);
        user.updateProfile(request.getName(), request.getIntroduction(), request.getImgUrl());
        return UserResponse.from(user);
    }

    @Override
    @Transactional
    public void deleteUser(String nanoId) {
        findActiveUserByNanoId(nanoId).deactivate();
    }

    // ── private helper ──────────────────────────────────────────────────────

    private User findActiveUserByNanoId(String nanoId) {
        return userRepository.findByNanoIdAndStatus(nanoId, UserStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }
}