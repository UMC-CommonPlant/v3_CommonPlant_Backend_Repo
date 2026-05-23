package com.commonplant.garden.user.dto;

import com.commonplant.garden.user.enums.Provider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class UserRequest {

    @Getter
    @NoArgsConstructor
    @Schema(name = "UserUpdateMultipartRequest", description = "사용자 정보 수정 multipart 요청")
    public static class UpdateMultipartRequest {
        @Schema(description = "수정할 사용자 정보(JSON)", implementation = UpdateRequest.class, nullable = true)
        private UpdateRequest user;

        @Schema(description = "사용자 프로필 이미지(선택)", type = "string", format = "binary", nullable = true)
        private String image;
    }

    @Getter
    @NoArgsConstructor
    @Schema(name = "UserUpdateRequest", description = "사용자 정보 수정 요청")
    public static class UpdateRequest {
        @Size(min = 1, max = 20, message = "이름은 1자 이상 20자 이하여야 합니다.")
        @Pattern(
                regexp = "^[a-zA-Z가-힣\\s]+$",
                message = "이름은 한글, 영문, 공백만 허용됩니다."
        )
        @Schema(description = "1~20자 사이 (허용: 한글, 영문, 공백)", example = "홍길동", nullable = true)
        private String name;

        @Size(max = 200, message = "소개는 200자를 초과할 수 없습니다.")
        @Pattern(
                regexp = "^[^\\\\<>\"'%;()&+]*$",
                message = "소개에 사용할 수 없는 특수문자가 포함되어 있습니다."
        )
        @Schema(description = "200자 이내 (비허용: <>\"'%;()&+\\)", example = "몬스테라를 키우는 식집사입니다.", nullable = true)
        private String introduction;

        @Schema(description = "기존 프로필 이미지 URL (새 이미지 업로드 시 무시됨)", example = "https://bucket.s3.ap-northeast-2.amazonaws.com/images/profile.png", nullable = true)
        private String imgUrl;
    }
}
