package com.commonplant.garden.s3.service;

import com.commonplant.garden.s3.dto.S3Response;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface S3Service {
    String getImageUrl(String key);

    S3Response.ImageInfo getImage(String nanoId, String key);

    void deleteImage(String nanoId, String key);

    S3Response.ImageInfo updateImage(String nanoId, String key, MultipartFile image);

    S3Response.ImageInfo updateImage(
            String nanoId,
            String key,
            ImagePath imagePath,
            MultipartFile image
    );

    S3Response.CompletedImages uploadImages(String nanoId, List<MultipartFile> images);

    S3Response.CompletedImages uploadImages(
            String nanoId,
            ImagePath imagePath,
            List<MultipartFile> images
    );

    S3Response.ImageInfo uploadImage(String nanoId, MultipartFile image);

    S3Response.ImageInfo uploadImage(String nanoId, ImagePath imagePath, MultipartFile image);

    default S3Response.ImageInfo uploadPlaceImage(
            String nanoId,
            String placeCode,
            MultipartFile image
    ) {
        return uploadImage(nanoId, ImagePath.place(placeCode), image);
    }

    default S3Response.ImageInfo uploadPlantImage(
            String nanoId,
            String placeCode,
            Long plantId,
            MultipartFile image
    ) {
        return uploadImage(nanoId, ImagePath.plant(placeCode, plantId), image);
    }

    default S3Response.ImageInfo uploadMemoImage(
            String nanoId,
            String placeCode,
            Long plantId,
            Long memoId,
            MultipartFile image
    ) {
        return uploadImage(nanoId, ImagePath.memo(placeCode, plantId, memoId), image);
    }

    default S3Response.ImageInfo uploadUserProfileImage(
            String nanoId,
            MultipartFile image
    ) {
        return uploadImage(nanoId, ImagePath.userProfile(nanoId), image);
    }

    default S3Response.ImageInfo updatePlaceImage(
            String nanoId,
            String key,
            String placeCode,
            MultipartFile image
    ) {
        return updateImage(nanoId, key, ImagePath.place(placeCode), image);
    }

    default S3Response.ImageInfo updatePlantImage(
            String nanoId,
            String key,
            String placeCode,
            Long plantId,
            MultipartFile image
    ) {
        return updateImage(nanoId, key, ImagePath.plant(placeCode, plantId), image);
    }

    default S3Response.ImageInfo updateMemoImage(
            String nanoId,
            String key,
            String placeCode,
            Long plantId,
            Long memoId,
            MultipartFile image
    ) {
        return updateImage(nanoId, key, ImagePath.memo(placeCode, plantId, memoId), image);
    }

    default S3Response.ImageInfo updateUserProfileImage(
            String nanoId,
            String key,
            MultipartFile image
    ) {
        return updateImage(nanoId, key, ImagePath.userProfile(nanoId), image);
    }
}
