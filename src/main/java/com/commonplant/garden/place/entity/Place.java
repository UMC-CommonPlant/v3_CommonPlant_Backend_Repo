package com.commonplant.garden.place.entity;

import com.commonplant.garden.common.domain.BaseTime;
import com.commonplant.garden.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Table(name = "place")
@NoArgsConstructor
@Entity
public class Place extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_idx")
    private Long placeIdx;

    @Column(nullable = false)     // 장소 이름
    private String name;

    @ManyToOne
    @JoinColumn(name = "user_idx", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User owner;

    @Column(nullable = true)
    private String gridX;    // 경도 (x)

    @Column(nullable = true)
    private String gridY;     // 위도 (y)

    @Column(nullable = false)
    private String address;

    @Column(nullable = true)
    private String imgUrl;

    @Column(nullable = false)
    private String code;

    @Builder
    public Place(String name, User owner, String gridX, String gridY, String address, String imgUrl, String code) {
        this.name = name;
        this.owner = owner;
        this.gridX = gridX;
        this.gridY = gridY;
        this.address = address;
        this.imgUrl = imgUrl;
        this.code = code;
    }

    public void setPlaceIdx(Long placeIdx) {
        this.placeIdx = placeIdx;
    }
}
