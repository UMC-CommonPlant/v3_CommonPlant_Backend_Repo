package com.commonplant.garden.s3.service;

import com.commonplant.garden.s3.dto.S3Request;
import com.commonplant.garden.s3.dto.S3Response;

public interface S3Service {
    S3Response.ImageInfo getImage(String nanoId, Long placeId, String key);

    void deleteImage(String nanoId, Long placeId, String key);

    S3Response.ImageInfo updateImage(String nanoId, Long placeId, String key, S3Request.UpdateImage request);

    S3Response.ImageUploadUrls createImageUploadUrls(String nanoId, S3Request.CreateImageUploadUrls request);

    S3Response.CompletedImages completeImageUpload(String nanoId, S3Request.CompleteImageUpload request);
}
