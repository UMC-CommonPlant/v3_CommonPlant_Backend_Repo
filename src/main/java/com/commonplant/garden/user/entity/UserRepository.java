package com.commonplant.garden.user.entity;

import com.commonplant.garden.user.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findAllByStatus(UserStatus status);

    Optional<User> findByUserIdxAndStatus(Long userIdx, UserStatus status);

    Optional<User> findByUuidAndStatus(String uuid, UserStatus status);

    boolean existsByEmail(String email);

    boolean existsByName(String name);
}
