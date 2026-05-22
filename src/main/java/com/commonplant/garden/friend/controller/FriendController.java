package com.commonplant.garden.friend.controller;

import com.commonplant.garden.common.dto.JsonResponse;
import com.commonplant.garden.friend.dto.FriendDto;
import com.commonplant.garden.friend.service.FriendService;
import com.commonplant.garden.user.dto.UserResponse;
import com.commonplant.garden.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/friends")
public class FriendController {

    private final FriendService friendService;
    private final UserService userService;

    @PostMapping("/request")
    public ResponseEntity<JsonResponse> sendFriendRequest(
            @AuthenticationPrincipal String nanoId,
            @RequestBody FriendDto.sendFriendReq req) {

        UserResponse response = userService.getUserByNanoId(nanoId);
        FriendDto.sendFriendRes res = friendService.sendFriendRequest(response.getId(), req);

        return ResponseEntity.ok(new JsonResponse(true, 200, "sendFriendRequest", res));
    }

    @PostMapping("/accept")
    public ResponseEntity<JsonResponse> acceptFriendRequest(
            @AuthenticationPrincipal String nanoId,
            @RequestBody FriendDto.friendDecisionReq req) {

        userService.getUserByNanoId(nanoId);
        friendService.acceptFriendRequest(nanoId, req);

        return ResponseEntity.ok(new JsonResponse(true, 200, "acceptFriendRequest", null));
    }

    @PostMapping("/decline")
    public ResponseEntity<JsonResponse> declineFriendRequest(
            @AuthenticationPrincipal String nanoId,
            @RequestBody FriendDto.friendDecisionReq req) {

        userService.getUserByNanoId(nanoId);
        friendService.declineFriendRequest(nanoId, req);

        return ResponseEntity.ok(new JsonResponse(true, 200, "declineFriendRequest", null));
    }

    @GetMapping("/requests")
    public ResponseEntity<JsonResponse> getFriendRequests(
            @AuthenticationPrincipal String nanoId) {

        userService.getUserByNanoId(nanoId);
        FriendDto.friendRequestListRes res = friendService.getFriendRequests(nanoId);

        return ResponseEntity.ok(new JsonResponse(true, 200, "getFriendRequests", res));
    }
}
