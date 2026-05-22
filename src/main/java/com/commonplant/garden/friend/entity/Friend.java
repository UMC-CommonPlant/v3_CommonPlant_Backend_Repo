package com.commonplant.garden.friend.entity;

import com.commonplant.garden.common.domain.BaseTime;
import com.commonplant.garden.friend.enums.FriendStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Getter
@Table(name = "friend")
@NoArgsConstructor
@Entity
public class Friend extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "friend_idx")
    private Long friendIdx;

    @Column(name = "sender")
    private String sender;

    @Column(name = "receiver")
    private String receiver;

    @Column(name = "place_code")
    private String placeCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private FriendStatus status;

    @Builder
    public Friend(String sender, String receiver, String placeCode, FriendStatus status) {
        this.sender = sender;
        this.receiver = receiver;
        this.placeCode = placeCode;
        this.status = status;
    }

    public void accept() {
        this.status = FriendStatus.ACCEPT;
    }

    public void decline() {
        this.status = FriendStatus.DECLINE;
    }
}
