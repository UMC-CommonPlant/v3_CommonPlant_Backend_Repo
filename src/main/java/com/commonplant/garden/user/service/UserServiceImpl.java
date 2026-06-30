package com.commonplant.garden.user.service;

import com.commonplant.garden.common.exception.BusinessException;
import com.commonplant.garden.s3.service.S3Service;
import com.commonplant.garden.user.dto.UserRequest;
import com.commonplant.garden.user.dto.UserResponse;
import com.commonplant.garden.user.entity.User;
import com.commonplant.garden.user.entity.UserRepository;
import com.commonplant.garden.user.enums.UserStatus;
import com.commonplant.garden.user.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final S3Service s3Service;

    @Override
    public UserResponse getUserByNanoId(String nanoId) {
        User user = findActiveUserByNanoId(nanoId);
        return UserResponse.from(user, resolveImageUrl(user.getImgUrl()));
    }

    @Override
    public List<UserResponse> searchUserByName(String keyword){
        return searchActiveUsersByUsername(keyword);
    }

    @Override
    @Transactional
    public UserResponse updateUser(String nanoId, UserRequest.UpdateRequest request, MultipartFile image) {
        User user = findActiveUserByNanoId(nanoId);

        user.updateProfile(
                request == null ? null : request.getName(),
                request == null ? null : request.getIntroduction()
        );

        // 이미지 파일이 함께 오면 교체하고, 없으면 기존 이미지를 그대로 둔다.
        if (hasFile(image)) {
            user.updateImageKey(uploadOrReplaceImage(nanoId, user.getImgUrl(), image));
        }

        return UserResponse.from(user, resolveImageUrl(user.getImgUrl()));
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
        return UserResponse.from(user, resolveImageUrl(user.getImgUrl()));
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
                .map(user -> UserResponse.from(user, resolveImageUrl(user.getImgUrl())))
                .collect(Collectors.toList());
    }

    // ── S3 helper ──────────────────────────────────────────────────────
    private boolean hasFile(MultipartFile image) {
        return image != null && !image.isEmpty();
    }

    /** 기존 이미지가 있으면 교체하고, 없으면 새로 업로드한 뒤 key 를 반환한다. */
    private String uploadOrReplaceImage(String nanoId, String existingImageKey, MultipartFile image) {
        if (StringUtils.hasText(existingImageKey)) {
            return s3Service.updateImage(nanoId, existingImageKey, image).getKey();
        }
        return s3Service.uploadImage(nanoId, image).getKey();
    }

    private String resolveImageUrl(String imageKey) {
        if (!StringUtils.hasText(imageKey)) {
            return null;
        }
        return s3Service.getImageUrl(imageKey);
    }
}