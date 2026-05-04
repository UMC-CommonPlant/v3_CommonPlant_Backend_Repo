package com.commonplant.garden.user.controller;

import com.commonplant.garden.common.dto.JsonResponse;
import com.commonplant.garden.common.util.JwtUtil;
import com.commonplant.garden.user.dto.UserRequest;
import com.commonplant.garden.user.dto.UserResponse;
import com.commonplant.garden.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<JsonResponse> getUserByNanoId(@AuthenticationPrincipal String nanoid) {
        UserResponse response = userService.getUserByNanoId(nanoid);
        return ResponseEntity.ok(new JsonResponse(true, 200, "getUserByNanoId", response));
    }

    @PostMapping
    public ResponseEntity<JsonResponse> createUser(@Valid @RequestBody UserRequest.CreateRequest request) {
        UserResponse response = userService.createUser(request);
        String accessToken = jwtUtil.generateAccessToken(response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(new JsonResponse(true, 200, "createUser", accessToken));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JsonResponse> updateUser(
            @PathVariable String id, @RequestBody UserRequest.UpdateRequest request) {
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(new JsonResponse(true, 200, "updateUser", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<JsonResponse> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new JsonResponse(true, 200, "deleteUser", null));
    }
}
