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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/s3")
public class S3Controller {

    private final S3Service s3Service;

    @GetMapping("/images/{imageId}")
    public ResponseEntity<JsonResponse> getImage(
            @AuthenticationPrincipal String nanoId,
            @PathVariable("imageId") Long imageId
    ) {
        S3Response.ImageInfo response = s3Service.getImage(nanoId, imageId);
        return ResponseEntity.ok(new JsonResponse(true, 200, "getImage", response));
    }

    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<JsonResponse> deleteImage(
            @AuthenticationPrincipal String nanoId,
            @PathVariable("imageId") Long imageId
    ) {
        s3Service.deleteImage(nanoId, imageId);
        return ResponseEntity.ok(new JsonResponse(true, 200, "deleteImage", null));
    }

    @PutMapping("/images/{imageId}")
    public ResponseEntity<JsonResponse> updateImage(
            @AuthenticationPrincipal String nanoId,
            @PathVariable("imageId") Long imageId,
            @Valid @RequestBody S3Request.UpdateImage request
    ) {
        S3Response.ImageInfo response = s3Service.updateImage(nanoId, imageId, request);
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
