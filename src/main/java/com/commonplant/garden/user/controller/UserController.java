package com.commonplant.garden.user.controller;

import com.commonplant.garden.user.entity.User;
import com.commonplant.garden.user.service.UserService;
import com.umc.commonplant.domain.user.dto.UserDto;

import com.umc.commonplant.global.dto.JsonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
public class UserController {
    private final UserService userService;

    @GetMapping("/user/{id}") // 회원정보 조회
    public ResponseEntity<JsonResponse> getUser(@PathVariable Long id){
        User user = userService.getUserById(id);
        return ResponseEntity.ok(new JsonResponse(true, 200, "사용자 조회 완료되었습니다.", user));
    }
}
