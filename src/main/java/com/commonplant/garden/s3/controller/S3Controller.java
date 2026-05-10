package com.commonplant.garden.s3.controller;

import com.commonplant.garden.common.dto.JsonResponse;
import com.commonplant.garden.s3.dto.S3Response;
import com.commonplant.garden.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/s3")
public class S3Controller {

    private final S3Service s3Service;

    @GetMapping("/images")
    public ResponseEntity<JsonResponse> getImage(
            @AuthenticationPrincipal String nanoId,
            @RequestParam("placeId") Long placeId,
            @RequestParam("key") String key
    ) {
        S3Response.ImageInfo response = s3Service.getImage(nanoId, placeId, key);
        return ResponseEntity.ok(new JsonResponse(true, 200, "getImage", response));
    }

    @DeleteMapping("/images")
    public ResponseEntity<JsonResponse> deleteImage(
            @AuthenticationPrincipal String nanoId,
            @RequestParam("placeId") Long placeId,
            @RequestParam("key") String key
    ) {
        s3Service.deleteImage(nanoId, placeId, key);
        return ResponseEntity.ok(new JsonResponse(true, 200, "deleteImage", null));
    }

    @PutMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JsonResponse> updateImage(
            @AuthenticationPrincipal String nanoId,
            @RequestParam("placeId") Long placeId,
            @RequestParam("key") String key,
            @RequestPart("image") MultipartFile image
    ) {
        S3Response.ImageInfo response = s3Service.updateImage(nanoId, placeId, key, image);
        return ResponseEntity.ok(new JsonResponse(true, 200, "updateImage", response));
    }

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JsonResponse> uploadImages(
            @AuthenticationPrincipal String nanoId,
            @RequestParam("placeId") Long placeId,
            @RequestPart("images") List<MultipartFile> images
    ) {
        S3Response.CompletedImages response = s3Service.uploadImages(nanoId, placeId, images);
        return ResponseEntity.ok(new JsonResponse(true, 200, "uploadImages", response));
    }
}
