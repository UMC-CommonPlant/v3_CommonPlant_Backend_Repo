package com.commonplant.garden.user.service;

import com.commonplant.garden.common.exception.BusinessException;
import com.commonplant.garden.user.entity.User;
import com.commonplant.garden.user.entity.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.commonplant.garden.user.exception.UserErrorCode.*;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;

    public User getUserById(Long id){ // User 조회
        return userRepository.findById(id).orElseThrow(() -> new BusinessException(NOT_FOUND_USER) {
        });
    }
}
