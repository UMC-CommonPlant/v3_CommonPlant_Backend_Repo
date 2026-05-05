package com.commonplant.garden.s3.entity;

import com.commonplant.garden.common.domain.BaseTime;
import com.commonplant.garden.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Table(name = "images")
@NoArgsConstructor
@Entity
public class Image extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_idx")
    private Long imageIdx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_idx", nullable = false)
    private User user;

    @Column(name = "place_idx", nullable = false)
    private Long placeId;

    @Column(name = "image_key", nullable = false, unique = true)
    private String imageKey;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Builder
    public Image(User user, Long placeId, String imageKey, String contentType, Long sizeBytes) {
        this.user = user;
        this.placeId = placeId;
        this.imageKey = imageKey;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
    }

    public void update(String imageKey, String contentType, Long sizeBytes) {
        this.imageKey = imageKey;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
    }
}
