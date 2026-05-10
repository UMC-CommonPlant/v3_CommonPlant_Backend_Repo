package com.commonplant.garden.belong.entity;

import com.commonplant.garden.place.entity.Place;
import com.commonplant.garden.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BelongRepository extends JpaRepository<Belong, Long> {

    @Query("select b.user from Belong b where b.place.code=?1")
    Optional<List<User>> getUserListByPlaceCode(String code);

    @Query("select b.user from Belong b where b.place.code=?1 order by b.createdAt")
    Optional<List<User>> getUserListByPlaceCodeOrderByCreatedAt(String code);

    @Query("select b.place from Belong b where b.user.nanoId=?1")
    List<Place> getPlaceListByUser(String nanoId);

    @Query("select b.place.placeIdx from Belong b where b.user.nanoId = ?1")
    List<Long> getPlaceIdsByUser(String nanoId);

    @Query("select count(b.belongIdx) from Belong b where b.user.nanoId = ?1 and b.place.code = ?2")
    Integer countUserOnPlace(String nanoId, String code);

    String countUserByPlace(Place place);

    @Query("select b from Belong b where b.user.nanoId=?1")
    List<Belong> getPlaceBelongUser(String nanoId);

    @Query("select b from Belong b where b.user.nanoId = ?1 and b.place.code = ?2")
    Optional<Belong> getBelongByUserAndPlace(String nanoId, String code);

    @Query("select count(b.belongIdx) from Belong b where b.place.code = ?1")
    Integer getNumberOfUserInPlace(String code);
}
