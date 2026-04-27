package com.commonplant.garden.user.service;

import com.commonplant.garden.common.exception.BusinessException;
import com.commonplant.garden.user.dto.UserRequest;
import com.commonplant.garden.user.dto.UserResponse;
import com.commonplant.garden.user.entity.User;
import com.commonplant.garden.user.entity.UserRepository;
import com.commonplant.garden.user.enums.Provider;
import com.commonplant.garden.user.enums.UserStatus;
import com.commonplant.garden.user.exception.UserErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User activeUser;

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
                .uuid("test-uuid")
                .name("홍길동")
                .email("hong@example.com")
                .provider(Provider.KAKAO)
                .providerId("kakao-123")
                .imgUrl(null)
                .build();
    }

    @Test
    @DisplayName("전체 활성 사용자 목록 조회")
    void getAllUsers_returnsActiveUsers() {
        given(userRepository.findAllByStatus(UserStatus.ACTIVE)).willReturn(List.of(activeUser));

        List<UserResponse> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("hong@example.com");
    }

    @Test
    @DisplayName("userIdx로 사용자 조회 성공")
    void getUserByIdx_success() {
        given(userRepository.findByUserIdxAndStatus(1L, UserStatus.ACTIVE))
                .willReturn(Optional.of(activeUser));

        UserResponse result = userService.getUserByIdx(1L);

        assertThat(result.getName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("존재하지 않는 userIdx 조회 시 USER_NOT_FOUND 예외")
    void getUserByIdx_notFound_throwsException() {
        given(userRepository.findByUserIdxAndStatus(99L, UserStatus.ACTIVE))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByIdx(99L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(UserErrorCode.USER_NOT_FOUND));
    }

    @Test
    @DisplayName("uuid로 사용자 조회 성공")
    void getUserByUuid_success() {
        given(userRepository.findByUuidAndStatus("test-uuid", UserStatus.ACTIVE))
                .willReturn(Optional.of(activeUser));

        UserResponse result = userService.getUserByUuid("test-uuid");

        assertThat(result.getUuid()).isEqualTo("test-uuid");
    }

    @Test
    @DisplayName("신규 사용자 생성 성공")
    void createUser_success() {
        UserRequest.CreateRequest request = new UserRequest.CreateRequest();
        setField(request, "name", "김철수");
        setField(request, "email", "kim@example.com");
        setField(request, "provider", Provider.GOOGLE);
        setField(request, "providerId", "google-456");
        setField(request, "imgUrl", null);

        given(userRepository.existsByEmail("kim@example.com")).willReturn(false);
        given(userRepository.existsByName("김철수")).willReturn(false);
        given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));

        UserResponse result = userService.createUser(request);

        assertThat(result.getEmail()).isEqualTo("kim@example.com");
        assertThat(result.getName()).isEqualTo("김철수");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("이메일 중복 시 DUPLICATE_EMAIL 예외")
    void createUser_duplicateEmail_throwsException() {
        UserRequest.CreateRequest request = new UserRequest.CreateRequest();
        setField(request, "name", "김철수");
        setField(request, "email", "hong@example.com");
        setField(request, "provider", Provider.GOOGLE);
        setField(request, "providerId", "google-456");

        given(userRepository.existsByEmail("hong@example.com")).willReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(UserErrorCode.DUPLICATE_EMAIL));
    }

    @Test
    @DisplayName("닉네임 중복 시 DUPLICATE_NICKNAME 예외")
    void createUser_duplicateNickname_throwsException() {
        UserRequest.CreateRequest request = new UserRequest.CreateRequest();
        setField(request, "name", "홍길동");
        setField(request, "email", "new@example.com");
        setField(request, "provider", Provider.KAKAO);
        setField(request, "providerId", "kakao-789");

        given(userRepository.existsByEmail("new@example.com")).willReturn(false);
        given(userRepository.existsByName("홍길동")).willReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(UserErrorCode.DUPLICATE_NICKNAME));
    }

    @Test
    @DisplayName("사용자 정보 수정 성공")
    void updateUser_success() {
        UserRequest.UpdateRequest request = new UserRequest.UpdateRequest();
        setField(request, "name", "수정된이름");
        setField(request, "introduction", "반갑습니다");
        setField(request, "imgUrl", "http://img.url/new.png");

        given(userRepository.findByUserIdxAndStatus(1L, UserStatus.ACTIVE))
                .willReturn(Optional.of(activeUser));

        UserResponse result = userService.updateUser(1L, request);

        assertThat(result.getName()).isEqualTo("수정된이름");
        assertThat(result.getIntroduction()).isEqualTo("반갑습니다");
    }

    @Test
    @DisplayName("사용자 삭제 시 status가 DELETED로 변경됨")
    void deleteUser_softDelete() {
        given(userRepository.findByUserIdxAndStatus(1L, UserStatus.ACTIVE))
                .willReturn(Optional.of(activeUser));

        userService.deleteUser(1L);

        assertThat(activeUser.getStatus()).isEqualTo(UserStatus.DELETED);
    }

    @Test
    @DisplayName("삭제할 사용자가 없으면 USER_NOT_FOUND 예외")
    void deleteUser_notFound_throwsException() {
        given(userRepository.findByUserIdxAndStatus(99L, UserStatus.ACTIVE))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(UserErrorCode.USER_NOT_FOUND));
    }

    // NoArgsConstructor로 생성된 DTO에 값을 주입하기 위한 헬퍼
    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
