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

import java.util.List;
import java.util.stream.Collectors;

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
    /** Active 사용자 조회 로직 - nanoId */
    public User findActiveUserByNanoId(String nanoId) {
        return userRepository.findByNanoIdAndStatus(nanoId, UserStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }

    // ── public helper ──────────────────────────────────────────────────────
    /** Active 사용자 조회 로직 - nanoId */
    public UserResponse searchActiveUserByNanoId(String nanoId) {
        User user = userRepository.findByNanoIdAndStatus(nanoId, UserStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }

    /** Active 사용자 조회 로직 - username */
    public List<UserResponse> searchActiveUsersByUsername(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new BusinessException(UserErrorCode.INVALID_SEARCH_KEYWORD);
        }

        List<User> users = userRepository.findByNameContainingAndStatus(keyword, UserStatus.ACTIVE);

        if (users.isEmpty()) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }

        return users.stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }
}