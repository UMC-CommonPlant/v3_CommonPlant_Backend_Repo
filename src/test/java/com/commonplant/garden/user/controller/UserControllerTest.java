package com.commonplant.garden.user.controller;

import com.commonplant.garden.common.exception.BusinessException;
import com.commonplant.garden.user.dto.UserRequest;
import com.commonplant.garden.user.dto.UserResponse;
import com.commonplant.garden.user.enums.Provider;
import com.commonplant.garden.user.enums.UserStatus;
import com.commonplant.garden.user.exception.UserErrorCode;
import com.commonplant.garden.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserResponse sampleResponse() {
        return UserResponse.builder()
                .userIdx(1L)
                .uuid("test-uuid")
                .name("홍길동")
                .email("hong@example.com")
                .status(UserStatus.ACTIVE)
                .provider(Provider.KAKAO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser
    @DisplayName("GET /users - 전체 사용자 목록 200 반환")
    void getAllUsers_returns200() throws Exception {
        given(userService.getAllUsers()).willReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("hong@example.com"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /users/{userIdx} - 단일 사용자 200 반환")
    void getUserByIdx_returns200() throws Exception {
        given(userService.getUserByIdx(1L)).willReturn(sampleResponse());

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("홍길동"))
                .andExpect(jsonPath("$.userIdx").value(1));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /users/{userIdx} - 없는 사용자 404 반환")
    void getUserByIdx_notFound_returns404() throws Exception {
        given(userService.getUserByIdx(99L))
                .willThrow(new BusinessException(UserErrorCode.USER_NOT_FOUND));

        mockMvc.perform(get("/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("U001"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /users - 사용자 생성 201 반환")
    void createUser_returns201() throws Exception {
        UserRequest.CreateRequest request = buildCreateRequest("홍길동", "hong@example.com", Provider.KAKAO, "kakao-123");
        given(userService.createUser(any())).willReturn(sampleResponse());

        mockMvc.perform(post("/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("hong@example.com"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /users - 이메일 중복 409 반환")
    void createUser_duplicateEmail_returns409() throws Exception {
        UserRequest.CreateRequest request = buildCreateRequest("홍길동", "hong@example.com", Provider.KAKAO, "kakao-123");
        given(userService.createUser(any()))
                .willThrow(new BusinessException(UserErrorCode.DUPLICATE_EMAIL));

        mockMvc.perform(post("/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("U002"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /users - 필수 필드 누락 400 반환")
    void createUser_missingField_returns400() throws Exception {
        String body = "{\"email\": \"hong@example.com\"}"; // name, provider, providerId 누락

        mockMvc.perform(post("/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /users/{userIdx} - 사용자 수정 200 반환")
    void updateUser_returns200() throws Exception {
        UserResponse updated = UserResponse.builder()
                .userIdx(1L)
                .uuid("test-uuid")
                .name("수정된이름")
                .email("hong@example.com")
                .introduction("안녕하세요")
                .status(UserStatus.ACTIVE)
                .provider(Provider.KAKAO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(userService.updateUser(eq(1L), any())).willReturn(updated);

        String body = "{\"name\": \"수정된이름\", \"introduction\": \"안녕하세요\"}";

        mockMvc.perform(put("/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("수정된이름"))
                .andExpect(jsonPath("$.introduction").value("안녕하세요"));
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /users/{userIdx} - 사용자 삭제 204 반환")
    void deleteUser_returns204() throws Exception {
        willDoNothing().given(userService).deleteUser(1L);

        mockMvc.perform(delete("/users/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /users/{userIdx} - 없는 사용자 삭제 시 404 반환")
    void deleteUser_notFound_returns404() throws Exception {
        willThrow(new BusinessException(UserErrorCode.USER_NOT_FOUND))
                .given(userService).deleteUser(99L);

        mockMvc.perform(delete("/users/99").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("U001"));
    }

    // ── helper ──────────────────────────────────────────────────────────────

    private UserRequest.CreateRequest buildCreateRequest(String name, String email,
                                                          Provider provider, String providerId) {
        try {
            UserRequest.CreateRequest req = new UserRequest.CreateRequest();
            setField(req, "name", name);
            setField(req, "email", email);
            setField(req, "provider", provider);
            setField(req, "providerId", providerId);
            return req;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
