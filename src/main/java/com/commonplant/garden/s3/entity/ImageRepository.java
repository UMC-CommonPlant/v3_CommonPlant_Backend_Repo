package com.commonplant.garden.s3.entity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
    boolean existsByImageKey(String imageKey);
}
