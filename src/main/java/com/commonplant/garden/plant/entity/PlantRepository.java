package com.commonplant.garden.plant.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;

public interface PlantRepository extends JpaRepository<Plant, Long> {
    Page<Plant> findAllByPlaceIdInOrderByPlantIdxDesc(Collection<Long> placeIds, Pageable pageable);

    List<Plant> findAllByPlaceIdOrderByPlantIdxDesc(Long placeId);
}
