package com.commonplant.garden.plant.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.Collection;
import java.util.List;

public interface PlantRepository extends JpaRepository<Plant, Long> {
    Slice<Plant> findAllByPlaceIdInOrderByPlantIdxDesc(Collection<Long> placeIds, Pageable pageable);

    List<Plant> findAllByPlaceIdOrderByPlantIdxDesc(Long placeId);
}
