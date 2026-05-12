package com.commonplant.garden.user.entity;

import com.commonplant.garden.user.enums.Provider;
import com.commonplant.garden.user.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByNanoIdAndStatus(String nanoId, UserStatus status);

    boolean existsByEmail(String email);

    boolean existsByProviderAndProviderId(Provider provider, String providerId);

    Optional<User> findByProviderAndProviderIdAndStatus(Provider provider, String providerId, UserStatus status);

    List<User> findByUsernameContainingAndStatus(String username, UserStatus status);
}
