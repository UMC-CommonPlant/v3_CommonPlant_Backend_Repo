package com.commonplant.garden.plant.entity;

import com.commonplant.garden.common.domain.BaseTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Table(name = "plants")
@NoArgsConstructor
@Entity
public class Plant extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plant_idx")
    private Long plantIdx;

    @Column(name = "place_idx", nullable = false)
    private Long placeId;

    @Column(name = "scientific_name_ko")
    private String scientificNameKo;

    @Column(name = "scientific_name_en")
    private String scientificNameEn;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "last_watered_date")
    private LocalDate lastWateredDate;

    @Column(name = "image_key")
    private String imageKey;

    @Column(name = "description")
    private String description;

    @Builder
    public Plant(
            Long placeId,
            String scientificNameKo,
            String scientificNameEn,
            String nickname,
            LocalDate lastWateredDate,
            String imageKey,
            String description
    ) {
        this.placeId = placeId;
        this.scientificNameKo = scientificNameKo;
        this.scientificNameEn = scientificNameEn;
        this.nickname = nickname;
        this.lastWateredDate = lastWateredDate;
        this.imageKey = imageKey;
        this.description = description;
    }

    public void updateProfile(String imageKey, String nickname, LocalDate lastWateredDate) {
        if (imageKey != null) this.imageKey = imageKey;
        if (nickname != null) this.nickname = nickname;
        if (lastWateredDate != null) this.lastWateredDate = lastWateredDate;
    }
}
