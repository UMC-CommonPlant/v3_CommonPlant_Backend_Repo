package com.commonplant.garden.user.controller;

import com.commonplant.garden.user.dto.UserRequest;
import com.commonplant.garden.user.dto.UserResponse;
import com.commonplant.garden.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{userIdx}")
    public ResponseEntity<UserResponse> getUserByIdx(@PathVariable Long userIdx) {
        return ResponseEntity.ok(userService.getUserByIdx(userIdx));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @PutMapping("/{userIdx}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long userIdx,
            @RequestBody UserRequest.UpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(userIdx, request));
    }

    @DeleteMapping("/{userIdx}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userIdx) {
        userService.deleteUser(userIdx);
        return ResponseEntity.noContent().build();
    }
}
