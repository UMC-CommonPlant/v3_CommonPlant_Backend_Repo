package com.commonplant.garden.user.controller;

import com.commonplant.garden.common.dto.JsonResponse;
import com.commonplant.garden.user.dto.UserRequest;
import com.commonplant.garden.user.dto.UserResponse;
import com.commonplant.garden.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "사용자 정보 API")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "[A003] 유효하지 않은 JWT | [A004] 만료된 JWT"),
            @ApiResponse(responseCode = "404", description = "[U101] 사용자를 찾을 수 없음"),
    })
    @GetMapping
    public ResponseEntity<JsonResponse> getUserByNanoId(@AuthenticationPrincipal String nanoId) {
        UserResponse response = userService.getUserByNanoId(nanoId);
        return ResponseEntity.ok(new JsonResponse(true, 200, "getUserByNanoId", response));
    }

    @Operation(summary = "내 정보 수정", description = "전달한 필드만 수정됩니다. 수정하지 않을 필드는 생략하세요.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "401", description = "[A003] 유효하지 않은 JWT | [A004] 만료된 JWT"),
            @ApiResponse(responseCode = "404", description = "[U101] 사용자를 찾을 수 없음"),
    })
    @PutMapping
    public ResponseEntity<JsonResponse> updateUser(
            @AuthenticationPrincipal String nanoId, @RequestBody UserRequest.UpdateRequest request) {
        UserResponse response = userService.updateUser(nanoId, request);
        return ResponseEntity.ok(new JsonResponse(true, 200, "updateUser", response));
    }

    @Operation(summary = "회원 탈퇴", description = "Soft delete 처리됩니다. (status: DELETED)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "탈퇴 성공"),
            @ApiResponse(responseCode = "401", description = "[A003] 유효하지 않은 JWT | [A004] 만료된 JWT"),
            @ApiResponse(responseCode = "404", description = "[U101] 사용자를 찾을 수 없음"),
    })
    @DeleteMapping
    public ResponseEntity<JsonResponse> deleteUser(@AuthenticationPrincipal String nanoId) {
        userService.deleteUser(nanoId);
        return ResponseEntity.ok(new JsonResponse(true, 200, "deleteUser", null));
    }
}
