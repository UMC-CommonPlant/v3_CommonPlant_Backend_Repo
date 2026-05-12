package com.commonplant.garden.s3.service;

import com.commonplant.garden.s3.dto.S3Response;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface S3Service {
    S3Response.ImageInfo getImage(String nanoId, String key);

    void deleteImage(String nanoId, String key);

    S3Response.ImageInfo updateImage(String nanoId, String key, MultipartFile image);

    S3Response.CompletedImages uploadImages(String nanoId, List<MultipartFile> images);

    S3Response.ImageInfo uploadImage(String nanoId, MultipartFile image);
}
