package com.commonplant.garden.s3.controller;

import com.commonplant.garden.common.dto.JsonResponse;
import com.commonplant.garden.s3.dto.S3Request;
import com.commonplant.garden.s3.dto.S3Response;
import com.commonplant.garden.s3.service.S3Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @PutMapping("/images")
    public ResponseEntity<JsonResponse> updateImage(
            @AuthenticationPrincipal String nanoId,
            @RequestParam("placeId") Long placeId,
            @RequestParam("key") String key,
            @Valid @RequestBody S3Request.UpdateImage request
    ) {
        S3Response.ImageInfo response = s3Service.updateImage(nanoId, placeId, key, request);
        return ResponseEntity.ok(new JsonResponse(true, 200, "updateImage", response));
    }

    @PostMapping("/images/presigned-urls")
    public ResponseEntity<JsonResponse> createImageUploadUrls(
            @AuthenticationPrincipal String nanoId,
            @Valid @RequestBody S3Request.CreateImageUploadUrls request
    ) {
        S3Response.ImageUploadUrls response = s3Service.createImageUploadUrls(nanoId, request);
        return ResponseEntity.ok(new JsonResponse(true, 200, "createImageUploadUrls", response));
    }

    @PostMapping("/images/complete")
    public ResponseEntity<JsonResponse> completeImageUpload(
            @AuthenticationPrincipal String nanoId,
            @Valid @RequestBody S3Request.CompleteImageUpload request
    ) {
        S3Response.CompletedImages response = s3Service.completeImageUpload(nanoId, request);
        return ResponseEntity.ok(new JsonResponse(true, 200, "completeImageUpload", response));
    }
}
