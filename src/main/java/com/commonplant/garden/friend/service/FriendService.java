package com.commonplant.garden.friend.service;

import com.commonplant.garden.friend.dto.FriendDto;

public interface FriendService {

    FriendDto.sendFriendRes sendFriendRequest(String nanoId, FriendDto.sendFriendReq req);

    void acceptFriendRequest(String nanoId, FriendDto.friendDecisionReq req);

    void declineFriendRequest(String nanoId, FriendDto.friendDecisionReq req);

    FriendDto.friendRequestListRes getFriendRequests(String nanoId);
}
