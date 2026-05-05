package com.commonplant.garden.belong.entity;

import com.commonplant.garden.common.domain.BaseTime;
import com.commonplant.garden.place.entity.Place;
import com.commonplant.garden.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import static jakarta.persistence.FetchType.LAZY;

@Getter
@Table(name = "belong")
@NoArgsConstructor
@Entity
public class Belong extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "belong_idx")
    private Long belongIdx;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_idx", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "place_idx", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Place place;

    @Builder
    public Belong(User user, Place place) {
        this.user = user;
        this.place = place;
    }
}
