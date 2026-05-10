package com.commonplant.garden.s3.service;

import com.commonplant.garden.s3.dto.S3Response;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface S3Service {
    S3Response.ImageInfo getImage(String nanoId, Long placeId, String key);

    void deleteImage(String nanoId, Long placeId, String key);

    S3Response.ImageInfo updateImage(String nanoId, Long placeId, String key, MultipartFile image);

    S3Response.CompletedImages uploadImages(String nanoId, Long placeId, List<MultipartFile> images);
}
