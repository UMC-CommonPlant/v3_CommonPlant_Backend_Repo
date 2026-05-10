package com.commonplant.garden.place.entity;

import com.commonplant.garden.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    Optional<Place> findByCode(String code);

    boolean existsByCode(String code);

    List<Place> findAllByOwner(User owner);

    @Query("select p.name from Place p where p.placeIdx = ?1")
    Optional<String> findNameById(Long placeId);
}
