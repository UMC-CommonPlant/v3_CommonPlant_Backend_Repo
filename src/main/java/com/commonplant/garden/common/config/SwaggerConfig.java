package com.commonplant.garden.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Encoding;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                )
                .addSecurityItem(new SecurityRequirement()
                        .addList("bearerAuth"))
                .info(apiInfo());
    }

    @Bean
    public OpenApiCustomizer multipartJsonPartCustomizer() {
        return openApi -> {
            addJsonPartEncoding(openApi, "/plants", PathItem.HttpMethod.POST, "plant");
            addJsonPartEncoding(openApi, "/plants/{plantId}", PathItem.HttpMethod.PUT, "plant");
            addBinaryPartEncoding(openApi, "/plants", PathItem.HttpMethod.POST, "image");
            addBinaryPartEncoding(openApi, "/plants/{plantId}", PathItem.HttpMethod.PUT, "image");
        };
    }

    private void addJsonPartEncoding(OpenAPI openApi, String path, PathItem.HttpMethod method, String partName) {
        if (openApi.getPaths() == null || openApi.getPaths().get(path) == null) {
            return;
        }

        var operation = openApi.getPaths().get(path).readOperationsMap().get(method);
        if (operation == null || operation.getRequestBody() == null || operation.getRequestBody().getContent() == null) {
            return;
        }

        var multipartContent = operation.getRequestBody().getContent().get(org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE);
        if (multipartContent == null) {
            return;
        }

        multipartContent.addEncoding(partName, new Encoding().contentType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE));
    }

    private void addBinaryPartEncoding(OpenAPI openApi, String path, PathItem.HttpMethod method, String partName) {
        if (openApi.getPaths() == null || openApi.getPaths().get(path) == null) {
            return;
        }

        var operation = openApi.getPaths().get(path).readOperationsMap().get(method);
        if (operation == null || operation.getRequestBody() == null || operation.getRequestBody().getContent() == null) {
            return;
        }

        var multipartContent = operation.getRequestBody().getContent().get(org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE);
        if (multipartContent == null) {
            return;
        }

        multipartContent.addEncoding(partName, new Encoding().contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE));
    }

    private Info apiInfo() {
        return new Info()
                .title("Common Plant API Document")
                .description("Common Plant API 명세서")
                .version("Version 1");
    }
}
