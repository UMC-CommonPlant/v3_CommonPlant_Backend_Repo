package com.commonplant.garden.common.config;

import com.commonplant.garden.common.filter.JwtAuthenticationFilter;
import com.commonplant.garden.common.security.JwtAccessDeniedHandler;
import com.commonplant.garden.common.security.JwtAuthenticationEntryPoint;
import com.commonplant.garden.common.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // 모바일 앱: 세션 / CSRF / 폼 로그인 모두 비활성화
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // 경로별 인증 규칙
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST,"/users").permitAll() // 사용자 토큰 발행 테스트
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/api-docs/**", "/api-docs/json/**", "/swagger-ui/**", "/swagger-ui.html").permitAll() // Swagger UI 접근 허용
                        .anyRequest().authenticated()
                )

                // JWT 예외 처리 핸들러 등록
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)  // 401: 미인증
                        .accessDeniedHandler(jwtAccessDeniedHandler)            // 403: 권한 없음
                )

                // JWT 검증 필터 등록
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtUtil, objectMapper),
                        UsernamePasswordAuthenticationFilter.class
                )
                .build();
    }
}
