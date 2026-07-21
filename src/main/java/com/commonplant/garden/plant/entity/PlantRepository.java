package com.commonplant.garden.plant.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface PlantRepository extends JpaRepository<Plant, Long> {
    Page<Plant> findAllByPlaceIdInOrderByPlantIdxDesc(Collection<Long> placeIds, Pageable pageable);

    List<Plant> findAllByPlaceIdOrderByPlantIdxDesc(Long placeId);

    @Query("select p.plantIdx from Plant p where p.placeId = ?1 order by p.plantIdx desc")
    List<Long> findPlantIdsByPlaceIdOrderByPlantIdxDesc(Long placeId);
}
