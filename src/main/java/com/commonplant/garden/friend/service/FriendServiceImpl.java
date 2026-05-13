package com.commonplant.garden.friend.service;

import com.commonplant.garden.belong.entity.Belong;
import com.commonplant.garden.belong.entity.BelongRepository;
import com.commonplant.garden.common.exception.BusinessException;
import com.commonplant.garden.friend.dto.FriendDto;
import com.commonplant.garden.friend.entity.Friend;
import com.commonplant.garden.friend.entity.FriendRepository;
import com.commonplant.garden.friend.enums.FriendStatus;
import com.commonplant.garden.friend.exception.FriendErrorCode;
import com.commonplant.garden.place.entity.Place;
import com.commonplant.garden.place.service.PlaceServiceImpl;
import com.commonplant.garden.user.dto.UserResponse;
import com.commonplant.garden.user.entity.User;
import com.commonplant.garden.user.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class FriendServiceImpl implements FriendService {

    private final UserServiceImpl userService;
    private final PlaceServiceImpl placeService;
    private final FriendRepository friendRepository;
    private final BelongRepository belongRepository;

    @Override
    public FriendDto.sendFriendRes sendFriendRequest(String nanoId, FriendDto.sendFriendReq req) {
        User sender = userService.findActiveUserByNanoId(nanoId);
        List<String> receiverList = req.getReceiverName();

        for (String name : receiverList) {
            UserResponse receiverRes = userService.searchActiveUsersByUsername(name).get(0);
            User receiver = userService.findActiveUserByNanoId(receiverRes.getId());

            if (belongRepository.countUserOnPlace(receiver.getNanoId(), req.getPlaceCode()) > 0) {
                throw new BusinessException(FriendErrorCode.USER_ALREADY_ON_PLACE);
            }
        }

        for (String name : receiverList) {
            Friend friend = Friend.builder()
                    .sender(sender.getName())
                    .receiver(name)
                    .placeCode(req.getPlaceCode())
                    .status(FriendStatus.PENDING)
                    .build();
            friendRepository.save(friend);
        }

        return new FriendDto.sendFriendRes(req.getPlaceCode(), receiverList);
    }

    @Override
    public void acceptFriendRequest(String nanoId, FriendDto.friendDecisionReq req) {
        User receiver = userService.findActiveUserByNanoId(nanoId);

        Friend friend = friendRepository.findByFriendIdxAndReceiver(req.getFriendId(), receiver.getName())
                .orElseThrow(() -> new BusinessException(FriendErrorCode.FRIEND_REQUEST_NOT_FOUND));

        friend.accept();

        Place place = placeService.getPlaceByCode(friend.getPlaceCode());

        if (belongRepository.countUserOnPlace(receiver.getNanoId(), place.getCode()) < 1) {
            belongRepository.save(Belong.builder().user(receiver).place(place).build());
        }
    }

    @Override
    public void declineFriendRequest(String nanoId, FriendDto.friendDecisionReq req) {
        User receiver = userService.findActiveUserByNanoId(nanoId);

        Friend friend = friendRepository.findByFriendIdxAndReceiver(req.getFriendId(), receiver.getName())
                .orElseThrow(() -> new BusinessException(FriendErrorCode.FRIEND_REQUEST_NOT_FOUND));

        friend.decline();
    }

    // 친구 요청 목록 조회
    @Override
    public FriendDto.friendRequestListRes getFriendRequests(String nanoId) {
        User receiver = userService.findActiveUserByNanoId(nanoId);

        List<Friend> requests = friendRepository.findByReceiverAndStatus(receiver.getName(), FriendStatus.PENDING);

        List<FriendDto.friendRequestItem> items = requests.stream().map(req -> {
            UserResponse senderRes = userService.searchActiveUsersByUsername(req.getSender()).get(0);
            User sender = userService.findActiveUserByNanoId(senderRes.getId());

            String placeName = placeService.getPlaceNameById(
                    placeService.getPlaceByCode(req.getPlaceCode()).getPlaceIdx()
            );
            String placeAddress = placeService.getPlaceAddressById(
                    placeService.getPlaceByCode(req.getPlaceCode()).getPlaceIdx()
            );

            return new FriendDto.friendRequestItem(
                    req.getFriendIdx(),
                    sender.getName(),
                    sender.getImgUrl(),
                    req.getPlaceCode(),
                    placeName,
                    placeAddress,
                    req.getStatus()
            );
        }).collect(Collectors.toList());

        return new FriendDto.friendRequestListRes(items);
    }
}
