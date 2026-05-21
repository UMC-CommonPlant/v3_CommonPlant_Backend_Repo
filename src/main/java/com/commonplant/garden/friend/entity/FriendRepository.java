package com.commonplant.garden.friend.entity;

import com.commonplant.garden.friend.enums.FriendStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    List<Friend> findByReceiverAndStatus(String receiver, FriendStatus status);

    List<Friend> findByReceiver(String receiver);

    Optional<Friend> findByFriendIdxAndReceiver(Long friendIdx, String receiver);
}
