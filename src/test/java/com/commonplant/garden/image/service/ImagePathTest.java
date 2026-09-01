package com.commonplant.garden.image.service;

import com.commonplant.garden.s3.service.ImagePath;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImagePathTest {

    @Test
    void createsDomainImageDirectories() {
        assertThat(ImagePath.place("PLACE01").directory())
                .isEqualTo("images/PLACE01");
        assertThat(ImagePath.plant("PLACE01", 42L).directory())
                .isEqualTo("images/PLACE01/plants/42");
        assertThat(ImagePath.memo("PLACE01", 42L, 7L).directory())
                .isEqualTo("images/PLACE01/plants/42/memos/7");
        assertThat(ImagePath.userProfile("user_nano-id").directory())
                .isEqualTo("images/users/user_nano-id");
        assertThat(ImagePath.legacy("user_nano-id").directory())
                .isEqualTo("images/user_nano-id");
    }

    @Test
    void derivesDirectoryFromExistingImageKey() {
        ImagePath imagePath = ImagePath.fromImageKey(
                "images/PLACE01/plants/42/941308d22fc5402298a91caa88a439e8.png"
        );

        assertThat(imagePath.directory()).isEqualTo("images/PLACE01/plants/42");
    }

    @Test
    void rejectsUnsafePathSegments() {
        assertThatThrownBy(() -> ImagePath.place("../other-place"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ImagePath.plant("PLACE01", 0L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
