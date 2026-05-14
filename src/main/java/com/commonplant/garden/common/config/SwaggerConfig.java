package com.commonplant.garden.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {

        // JWT Bearer 인증 방식
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        // 전역 Security 적용
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearerAuth");

        return new OpenAPI()
                .info(apiInfo())
                .addSecurityItem(securityRequirement)
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", securityScheme))
                .addServersItem(
                        new Server().url("/api/v1") // 기본 엔드포인트 적용
                );
    }

    private Info apiInfo() {
        return new Info()
                .title("Common Plant API Document")
                .description("Common Plant API 명세서")
                .version("Version 1");
    }

    /**
     * Swagger UI 기본 Response 제거
     * (JsonResponse / ErrorResponse 래핑 구조 대응)
     */
    @Bean
    public OperationCustomizer operationCustomizer() {
        return (operation, handlerMethod) -> {

            if (operation.getResponses() == null) {
                return operation;
            }

            operation.getResponses().forEach((responseCode, response) -> {

                if (response.getContent() != null) {

                    boolean hasExamples = response.getContent()
                            .values()
                            .stream()
                            .anyMatch(mediaType ->
                                    mediaType.getExamples() != null &&
                                            !mediaType.getExamples().isEmpty()
                            );

                    // examples 없는 response는 Swagger UI에서 제거
                    if (!hasExamples) {
                        response.setContent(null);
                    }
                }
            });

            return operation;
        };
    }
}
