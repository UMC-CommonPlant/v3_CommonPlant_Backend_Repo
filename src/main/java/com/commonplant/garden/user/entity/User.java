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

    // 프로필 이미지의 S3 객체 key 를 저장한다. 응답 시 presigned URL 로 변환한다.
    @Column(name = "img_url", nullable = true)
    private String imgUrl;

    @Column(name = "introduction", nullable = true)
    private String introduction;

    // 소셜 로그인 신규 유저 생성
    @Builder
    public User(String nanoId, String name, String email, String introduction,
                Provider provider, String providerId) {
        this.nanoId = nanoId;
        this.name = name;
        this.email = email;
        this.introduction = introduction;
        this.provider = provider;
        this.providerId = providerId;
        this.status = UserStatus.ACTIVE;
    }

    public void updateProfile(String name, String introduction) {
        if (name != null) this.name = name;
        if (introduction != null) this.introduction = introduction;
    }


    /** 프로필 이미지 key 교체. null 이면 이미지 제거를 의미한다. */
    public void updateImageKey(String imageKey) {
        this.imgUrl = imageKey;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void deactivate() {
        this.status = UserStatus.DELETED;
    }
}
