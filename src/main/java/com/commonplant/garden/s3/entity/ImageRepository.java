package com.commonplant.garden.s3.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {
    boolean existsByImageKey(String imageKey);

    Optional<Image> findByImageKeyAndPlaceId(String imageKey, Long placeId);
}
