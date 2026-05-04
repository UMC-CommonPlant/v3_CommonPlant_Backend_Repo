package com.commonplant.garden.user.entity;

import com.commonplant.garden.common.domain.BaseTime;
import com.commonplant.garden.user.enums.Provider;
import com.commonplant.garden.user.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Table(name = "users")
@NoArgsConstructor
@Entity
public class User extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_idx")
    private Long userIdx;

    @Column(name = "nano_id", nullable = false, unique = true)
    private String nanoId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private UserStatus status;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider")
    private Provider provider;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "img_url", nullable = true)
    private String imgUrl;

    @Column(name = "introduction", nullable = true)
    private String introduction;

    // 소셜 로그인 신규 유저 생성
    @Builder
    public User(String nanoId, String name, String email, String introduction,
                Provider provider, String providerId, String imgUrl) {
        this.nanoId = nanoId;
        this.name = name;
        this.email = email;
        this.introduction = introduction;
        this.provider = provider;
        this.providerId = providerId;
        this.imgUrl = imgUrl;
        this.status = UserStatus.ACTIVE;
    }

    public void updateProfile(String name, String introduction, String imgUrl) {
        if (name != null) this.name = name;
        if (introduction != null) this.introduction = introduction;
        if (imgUrl != null) this.imgUrl = imgUrl;
    }

    public void deactivate() {
        this.status = UserStatus.DELETED;
    }
}
