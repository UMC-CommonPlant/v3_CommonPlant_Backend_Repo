package com.commonplant.garden.user.controller;

import com.commonplant.garden.common.dto.JsonResponse;
import com.commonplant.garden.user.dto.UserRequest;
import com.commonplant.garden.user.dto.UserResponse;
import com.commonplant.garden.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController implements UserControllerDocs {

    private final UserService userService;

    @Override
    @GetMapping("/{keyword}")
    public ResponseEntity<JsonResponse> searchUserByName(@PathVariable("keyword") String keyword) {
        List<UserResponse> response = userService.searchUserByName(keyword);
        return ResponseEntity.ok(new JsonResponse(true, 200, "searchUserByName", response));
    }

    @Override
    @GetMapping
    public ResponseEntity<JsonResponse> getUserByNanoId(@AuthenticationPrincipal String nanoId) {
        UserResponse response = userService.getUserByNanoId(nanoId);
        return ResponseEntity.ok(new JsonResponse(true, 200, "getUserByNanoId", response));
    }

    @Override
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonResponse> updateUser(
            @AuthenticationPrincipal String nanoId,
            @RequestBody UserRequest.UpdateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        UserResponse response = userService.updateUser(nanoId, request);
        return ResponseEntity.ok(new JsonResponse(true, 200, "updateUser", response));
    }

    @Override
    @DeleteMapping
    public ResponseEntity<JsonResponse> deleteUser(@AuthenticationPrincipal String nanoId) {
        userService.deleteUser(nanoId);
        return ResponseEntity.ok(new JsonResponse(true, 200, "deleteUser", null));
    }
}
