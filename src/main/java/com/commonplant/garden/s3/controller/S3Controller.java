package com.commonplant.garden.s3.controller;

import com.commonplant.garden.common.dto.JsonResponse;
import com.commonplant.garden.s3.dto.S3Response;
import com.commonplant.garden.s3.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Image", description = "Garage 이미지 업로드/조회/수정/삭제 API")
public class S3Controller {

    private final S3Service s3Service;

    @Operation(
            summary = "이미지 다운로드 URL 조회",
            description = "이미지 key로 접근 가능한 presigned download URL을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이미지 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "이미지 없음")
    })
    @GetMapping("/images")
    public ResponseEntity<JsonResponse> getImage(
            @Parameter(hidden = true) @AuthenticationPrincipal String nanoId,
            @Parameter(description = "이미지 key", example = "images/user-nano-id/sample.png")
            @RequestParam("key") String key
    ) {
        S3Response.ImageInfo response = s3Service.getImage(nanoId, key);
        return ResponseEntity.ok(new JsonResponse(true, 200, "getImage", response));
    }

    @Operation(summary = "이미지 삭제", description = "이미지 key에 해당하는 Garage 객체와 이미지 메타데이터를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이미지 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "이미지 없음")
    })
    @DeleteMapping("/images")
    public ResponseEntity<JsonResponse> deleteImage(
            @Parameter(hidden = true) @AuthenticationPrincipal String nanoId,
            @Parameter(description = "이미지 key", example = "images/user-nano-id/sample.png")
            @RequestParam("key") String key
    ) {
        s3Service.deleteImage(nanoId, key);
        return ResponseEntity.ok(new JsonResponse(true, 200, "deleteImage", null));
    }

    @Operation(
            summary = "이미지 수정",
            description = "기존 이미지 key에 해당하는 이미지를 새 이미지 파일로 교체합니다. 허용 타입은 jpeg, png, webp이고 최대 크기는 10MB입니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이미지 수정 성공"),
            @ApiResponse(responseCode = "400", description = "이미지 파일 형식/크기 오류"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "이미지 없음")
    })
    @PutMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JsonResponse> updateImage(
            @Parameter(hidden = true) @AuthenticationPrincipal String nanoId,
            @Parameter(description = "교체 대상 이미지 key", example = "images/user-nano-id/sample.png")
            @RequestParam("key") String key,
            @Parameter(
                    description = "교체할 이미지 파일",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary"))
            )
            @RequestPart("image") MultipartFile image
    ) {
        S3Response.ImageInfo response = s3Service.updateImage(nanoId, key, image);
        return ResponseEntity.ok(new JsonResponse(true, 200, "updateImage", response));
    }

    @Operation(
            summary = "이미지 다중 업로드",
            description = "이미지 파일을 1개 이상, 최대 5개까지 업로드합니다. 허용 타입은 jpeg, png, webp이고 파일당 최대 크기는 10MB입니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이미지 업로드 성공"),
            @ApiResponse(responseCode = "400", description = "이미지 개수/파일 형식/크기 오류")
    })
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JsonResponse> uploadImages(
            @Parameter(hidden = true) @AuthenticationPrincipal String nanoId,
            @Parameter(
                    description = "업로드할 이미지 파일 목록(1~5개)",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            array = @ArraySchema(schema = @Schema(type = "string", format = "binary")))
            )
            @RequestPart("images") List<MultipartFile> images
    ) {
        S3Response.CompletedImages response = s3Service.uploadImages(nanoId, images);
        return ResponseEntity.ok(new JsonResponse(true, 200, "uploadImages", response));
    }
}
