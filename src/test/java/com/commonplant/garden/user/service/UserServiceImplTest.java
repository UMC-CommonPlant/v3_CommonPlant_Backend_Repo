package com.commonplant.garden.user.service;

import com.commonplant.garden.common.exception.BusinessException;
import com.commonplant.garden.s3.service.S3Service;
import com.commonplant.garden.user.dto.UserResponse;
import com.commonplant.garden.user.entity.User;
import com.commonplant.garden.user.entity.UserRepository;
import com.commonplant.garden.user.enums.Provider;
import com.commonplant.garden.user.enums.UserStatus;
import com.commonplant.garden.user.exception.UserErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final String NANO_ID = "user-nano-id";

    @Mock
    private UserRepository userRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getUserByNanoId_returnsActiveUser() {
        User user = createUser("홍길동");
        given(userRepository.findByNanoIdAndStatus(NANO_ID, UserStatus.ACTIVE))
                .willReturn(Optional.of(user));

        UserResponse response = userService.getUserByNanoId(NANO_ID);

        assertThat(response.getId()).isEqualTo(NANO_ID);
        assertThat(response.getName()).isEqualTo("홍길동");
        assertThat(response.getImgUrl()).isNull();
        verifyNoInteractions(s3Service);
    }

    @Test
    void getUserByNanoId_throwsUserNotFound_whenActiveUserDoesNotExist() {
        given(userRepository.findByNanoIdAndStatus(NANO_ID, UserStatus.ACTIVE))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByNanoId(NANO_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    void searchUserByName_returnsOnlyRepositoryResults() {
        User first = createUser("홍길동");
        User second = createUser("홍길순");
        given(userRepository.findByNameContainingAndStatus("홍길", UserStatus.ACTIVE))
                .willReturn(List.of(first, second));

        List<UserResponse> responses = userService.searchUserByName("홍길");

        assertThat(responses)
                .extracting(UserResponse::getName)
                .containsExactly("홍길동", "홍길순");
        verifyNoInteractions(s3Service);
    }

    @Test
    void searchUserByName_throwsInvalidSearchKeyword_whenKeywordIsBlank() {
        assertThatThrownBy(() -> userService.searchUserByName(" "))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.INVALID_SEARCH_KEYWORD);

        verifyNoInteractions(userRepository, s3Service);
    }

    @Test
    void updateUser_keepsProfile_whenRequestAndImageAreAbsent() {
        User user = createUser("홍길동");
        given(userRepository.findByNanoIdAndStatus(NANO_ID, UserStatus.ACTIVE))
                .willReturn(Optional.of(user));

        UserResponse response = userService.updateUser(NANO_ID, null, null);

        assertThat(response.getName()).isEqualTo("홍길동");
        assertThat(response.getImgUrl()).isNull();
        verifyNoInteractions(s3Service);
    }

    @Test
    void deleteUser_softDeletesActiveUser() {
        User user = createUser("홍길동");
        given(userRepository.findByNanoIdAndStatus(NANO_ID, UserStatus.ACTIVE))
                .willReturn(Optional.of(user));

        userService.deleteUser(NANO_ID);

        assertThat(user.getStatus()).isEqualTo(UserStatus.DELETED);
        verify(userRepository).findByNanoIdAndStatus(NANO_ID, UserStatus.ACTIVE);
    }

    private User createUser(String name) {
        return User.builder()
                .nanoId(NANO_ID)
                .name(name)
                .email(name + "@example.com")
                .introduction("식물을 키우고 있습니다.")
                .provider(Provider.GOOGLE)
                .providerId("provider-id")
                .build();
    }
}
