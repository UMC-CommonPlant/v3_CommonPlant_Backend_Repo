package com.commonplant.garden.image.service;

import com.commonplant.garden.common.config.GarageProperties;
import com.commonplant.garden.s3.dto.S3Response;
import com.commonplant.garden.s3.entity.Image;
import com.commonplant.garden.s3.entity.ImageRepository;
import com.commonplant.garden.user.entity.User;
import com.commonplant.garden.user.entity.UserRepository;
import com.commonplant.garden.user.enums.Provider;
import com.commonplant.garden.user.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GarageImageServiceTest {

    private static final String NANO_ID = "user-nano-id";
    private static final String EXISTING_IMAGE_KEY = "images/user-nano-id/original.png";

    @Mock
    private S3Client garageClient;

    @Mock
    private S3Presigner garagePresigner;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private UserRepository userRepository;

    private GarageImageService imageService;

    @BeforeEach
    void setUp() {
        GarageProperties properties = new GarageProperties(
                "http://localhost:3900",
                "garage",
                "test-access-key",
                "test-secret-key",
                "test-bucket",
                true,
                10,
                new GarageProperties.Image(5, 10_000_000, List.of("image/jpeg", "image/png", "image/webp"))
        );
        imageService = new GarageImageService(
                garageClient,
                garagePresigner,
                properties,
                imageRepository,
                userRepository
        );
    }

    @Test
    void garagePropertiesUseLocalDefaults() {
        GarageProperties properties = new GarageProperties(
                null,
                null,
                "test-access-key",
                "test-secret-key",
                null,
                null,
                0,
                null
        );

        assertThat(properties.endpoint()).isEqualTo("http://localhost:3900");
        assertThat(properties.region()).isEqualTo("garage");
        assertThat(properties.bucketName()).isEqualTo("commonplant-local");
        assertThat(properties.pathStyleAccessEnabled()).isTrue();
        assertThat(properties.presignedUrlExpirationMinutes()).isEqualTo(10);
        assertThat(properties.image().maxUploadCount()).isEqualTo(5);
        assertThat(properties.image().maxSizeBytes()).isEqualTo(10_485_760);
        assertThat(properties.image().allowedContentTypes())
                .containsExactly("image/jpeg", "image/png", "image/webp");
    }

    @Test
    void uploadImageStoresObjectAndMetadata() {
        User user = createUser();
        MockMultipartFile imageFile = createImageFile("profile.png");
        when(userRepository.findByNanoIdAndStatus(NANO_ID, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(imageRepository.save(any(Image.class))).thenAnswer(invocation -> invocation.getArgument(0));

        S3Response.ImageInfo response = imageService.uploadImage(NANO_ID, imageFile);

        assertThat(response.getKey())
                .startsWith("images/" + NANO_ID + "/")
                .endsWith(".png");
        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(garageClient).putObject(requestCaptor.capture(), any(RequestBody.class));
        assertThat(requestCaptor.getValue().bucket()).isEqualTo("test-bucket");
        assertThat(requestCaptor.getValue().key()).isEqualTo(response.getKey());
        verify(imageRepository).save(any(Image.class));
    }

    @Test
    void getImageUsesGaragePresigner() throws Exception {
        User user = createUser();
        Image image = createImage(user);
        String expectedUrl = "http://localhost:3900/test-bucket/" + EXISTING_IMAGE_KEY;
        PresignedGetObjectRequest presignedRequest = mock(PresignedGetObjectRequest.class);
        when(userRepository.findByNanoIdAndStatus(NANO_ID, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(imageRepository.findByImageKey(EXISTING_IMAGE_KEY)).thenReturn(Optional.of(image));
        when(presignedRequest.url()).thenReturn(URI.create(expectedUrl).toURL());
        when(garagePresigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .thenReturn(presignedRequest);

        S3Response.ImageInfo response = imageService.getImage(NANO_ID, EXISTING_IMAGE_KEY);

        assertThat(response.getDownloadUrl()).isEqualTo(expectedUrl);
        verify(imageRepository).findByImageKey(EXISTING_IMAGE_KEY);
        verify(garagePresigner).presignGetObject(any(GetObjectPresignRequest.class));
    }

    @Test
    void deleteImageDefersObjectRemovalUntilCommit() {
        User user = createUser();
        Image image = createImage(user);
        when(userRepository.findByNanoIdAndStatus(NANO_ID, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(imageRepository.findByImageKey(EXISTING_IMAGE_KEY)).thenReturn(Optional.of(image));
        TransactionSynchronizationManager.initSynchronization();

        try {
            imageService.deleteImage(NANO_ID, EXISTING_IMAGE_KEY);

            verify(imageRepository).delete(image);
            verify(garageClient, never()).deleteObject(any(DeleteObjectRequest.class));

            List<TransactionSynchronization> synchronizations =
                    TransactionSynchronizationManager.getSynchronizations();
            synchronizations.forEach(TransactionSynchronization::afterCommit);
            synchronizations.forEach(synchronization ->
                    synchronization.afterCompletion(TransactionSynchronization.STATUS_COMMITTED));

            ArgumentCaptor<DeleteObjectRequest> requestCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
            verify(garageClient).deleteObject(requestCaptor.capture());
            assertThat(requestCaptor.getValue().key()).isEqualTo(EXISTING_IMAGE_KEY);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void uploadedObjectIsRemovedWhenTransactionRollsBack() {
        User user = createUser();
        MockMultipartFile imageFile = createImageFile("profile.png");
        when(userRepository.findByNanoIdAndStatus(NANO_ID, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(imageRepository.save(any(Image.class))).thenAnswer(invocation -> invocation.getArgument(0));
        TransactionSynchronizationManager.initSynchronization();

        try {
            S3Response.ImageInfo response = imageService.uploadImage(NANO_ID, imageFile);

            verify(garageClient, never()).deleteObject(any(DeleteObjectRequest.class));
            TransactionSynchronizationManager.getSynchronizations().forEach(synchronization ->
                    synchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));

            ArgumentCaptor<DeleteObjectRequest> requestCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
            verify(garageClient).deleteObject(requestCaptor.capture());
            assertThat(requestCaptor.getValue().key()).isEqualTo(response.getKey());
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void emptyImageListIsRejectedBeforeStorageAccess() {
        User user = createUser();
        when(userRepository.findByNanoIdAndStatus(NANO_ID, UserStatus.ACTIVE)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> imageService.uploadImages(NANO_ID, List.of()))
                .isInstanceOf(com.commonplant.garden.common.exception.BusinessException.class);

        verifyNoInteractions(imageRepository, garageClient, garagePresigner);
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
