package com.commonplant.garden.image.service;

import com.commonplant.garden.common.config.MinioProperties;
import com.commonplant.garden.s3.dto.S3Response;
import com.commonplant.garden.s3.entity.Image;
import com.commonplant.garden.s3.entity.ImageRepository;
import com.commonplant.garden.user.entity.User;
import com.commonplant.garden.user.entity.UserRepository;
import com.commonplant.garden.user.enums.Provider;
import com.commonplant.garden.user.enums.UserStatus;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MinioImageServiceTest {

    private static final String NANO_ID = "user-nano-id";
    private static final String EXISTING_IMAGE_KEY = "images/user-nano-id/original.png";

    @Mock
    private MinioClient minioClient;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private UserRepository userRepository;

    private MinioImageService imageService;

    @BeforeEach
    void setUp() {
        MinioProperties properties = new MinioProperties(
                "http://localhost:9000",
                "test-access-key",
                "test-secret-key",
                "test-bucket",
                10,
                new MinioProperties.Image(5, 10_000_000, List.of("image/jpeg", "image/png", "image/webp"))
        );
        imageService = new MinioImageService(minioClient, properties, imageRepository, userRepository);
    }

    @Test
    void minioPropertiesUseImageDefaultsWithoutApplicationYamlChanges() {
        MinioProperties properties = new MinioProperties(
                "http://localhost:9000",
                "test-access-key",
                "test-secret-key",
                "test-bucket",
                0,
                null
        );

        assertThat(properties.bucketName()).isEqualTo("test-bucket");
        assertThat(properties.presignedUrlExpirationMinutes()).isEqualTo(10);
        assertThat(properties.image().maxUploadCount()).isEqualTo(5);
        assertThat(properties.image().maxSizeBytes()).isEqualTo(10_485_760);
        assertThat(properties.image().allowedContentTypes())
                .containsExactly("image/jpeg", "image/png", "image/webp");
    }

    @Test
    void uploadImageStoresObjectAndMetadata() throws Exception {
        User user = createUser();
        MockMultipartFile imageFile = createImageFile("profile.png");
        when(userRepository.findByNanoIdAndStatus(NANO_ID, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(imageRepository.save(any(Image.class))).thenAnswer(invocation -> invocation.getArgument(0));

        S3Response.ImageInfo response = imageService.uploadImage(NANO_ID, imageFile);

        assertThat(response.getKey())
                .startsWith("images/" + NANO_ID + "/")
                .endsWith(".png");
        ArgumentCaptor<PutObjectArgs> requestCaptor = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient).putObject(requestCaptor.capture());
        assertThat(requestCaptor.getValue().bucket()).isEqualTo("test-bucket");
        assertThat(requestCaptor.getValue().object()).isEqualTo(response.getKey());
        verify(imageRepository).save(any(Image.class));
    }

    @Test
    void getImageUsesOneMetadataLookup() throws Exception {
        User user = createUser();
        Image image = createImage(user);
        String expectedUrl = "http://localhost:9000/test-bucket/" + EXISTING_IMAGE_KEY;
        when(userRepository.findByNanoIdAndStatus(NANO_ID, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(imageRepository.findByImageKey(EXISTING_IMAGE_KEY)).thenReturn(Optional.of(image));
        when(minioClient.getPresignedObjectUrl(any())).thenReturn(expectedUrl);

        S3Response.ImageInfo response = imageService.getImage(NANO_ID, EXISTING_IMAGE_KEY);

        assertThat(response.getDownloadUrl()).isEqualTo(expectedUrl);
        verify(imageRepository).findByImageKey(EXISTING_IMAGE_KEY);
    }

    @Test
    void deleteImageDefersObjectRemovalUntilCommit() throws Exception {
        User user = createUser();
        Image image = createImage(user);
        when(userRepository.findByNanoIdAndStatus(NANO_ID, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(imageRepository.findByImageKey(EXISTING_IMAGE_KEY)).thenReturn(Optional.of(image));
        TransactionSynchronizationManager.initSynchronization();

        try {
            imageService.deleteImage(NANO_ID, EXISTING_IMAGE_KEY);

            verify(imageRepository).delete(image);
            verify(minioClient, never()).removeObject(any());

            List<TransactionSynchronization> synchronizations =
                    TransactionSynchronizationManager.getSynchronizations();
            synchronizations.forEach(TransactionSynchronization::afterCommit);
            synchronizations.forEach(synchronization ->
                    synchronization.afterCompletion(TransactionSynchronization.STATUS_COMMITTED));

            ArgumentCaptor<RemoveObjectArgs> requestCaptor = ArgumentCaptor.forClass(RemoveObjectArgs.class);
            verify(minioClient).removeObject(requestCaptor.capture());
            assertThat(requestCaptor.getValue().object()).isEqualTo(EXISTING_IMAGE_KEY);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void uploadedObjectIsRemovedWhenTransactionRollsBack() throws Exception {
        User user = createUser();
        MockMultipartFile imageFile = createImageFile("profile.png");
        when(userRepository.findByNanoIdAndStatus(NANO_ID, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(imageRepository.save(any(Image.class))).thenAnswer(invocation -> invocation.getArgument(0));
        TransactionSynchronizationManager.initSynchronization();

        try {
            S3Response.ImageInfo response = imageService.uploadImage(NANO_ID, imageFile);

            verify(minioClient, never()).removeObject(any());
            TransactionSynchronizationManager.getSynchronizations().forEach(synchronization ->
                    synchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));

            ArgumentCaptor<RemoveObjectArgs> requestCaptor = ArgumentCaptor.forClass(RemoveObjectArgs.class);
            verify(minioClient).removeObject(requestCaptor.capture());
            assertThat(requestCaptor.getValue().object()).isEqualTo(response.getKey());
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void emptyImageListIsRejectedBeforeStorageAccess() {
        User user = createUser();
        when(userRepository.findByNanoIdAndStatus(NANO_ID, UserStatus.ACTIVE)).thenReturn(Optional.of(user));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> imageService.uploadImages(NANO_ID, List.of()))
                .isInstanceOf(com.commonplant.garden.common.exception.BusinessException.class);

        verifyNoInteractions(imageRepository, minioClient);
    }

    private MockMultipartFile createImageFile(String fileName) {
        return new MockMultipartFile("image", fileName, "image/png", new byte[]{1, 2, 3});
    }

    private Image createImage(User user) {
        return Image.builder()
                .user(user)
                .imageKey(EXISTING_IMAGE_KEY)
                .contentType("image/png")
                .sizeBytes(1024L)
                .build();
    }

    private User createUser() {
        return User.builder()
                .nanoId(NANO_ID)
                .name("tester")
                .email("tester@example.com")
                .provider(Provider.GOOGLE)
                .providerId("provider-id")
                .build();
    }
}
