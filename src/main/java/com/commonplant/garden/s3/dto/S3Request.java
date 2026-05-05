package com.commonplant.garden.s3.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class S3Request {

    @Getter
    @NoArgsConstructor
    public static class CreateImageUploadUrls {
        @NotEmpty
        private List<ImageFile> files;
    }

    @Getter
    @NoArgsConstructor
    public static class CompleteImageUpload {
        @NotEmpty
        private List<String> keys;
    }

    @Getter
    @NoArgsConstructor
    public static class ImageFile {
        @NotBlank
        private String fileName;

        @NotBlank
        private String contentType;
    }
}
