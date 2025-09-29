package com.jwtauth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("JWT Authentication API")
                        .description("""
                                ## Spring Boot 기반 JWT 인증 시스템 REST API
                                
                                이 API는 완전한 JWT 기반 인증 및 권한 관리 시스템을 제공합니다.
                                
                                ### 주요 기능
                                - 사용자 회원가입 및 로그인
                                - JWT Access Token & Refresh Token 관리
                                - 역할 기반 권한 제어 (ADMIN, MODERATOR, USER)
                                - 보안 기능 (Rate Limiting, Login Attempt Tracking)
                                - 사용자 프로필 관리 및 비밀번호 변경
                                - 관리자 기능 (사용자 관리, 시스템 모니터링)
                                
                                ### 인증 방법
                                1. `/api/auth/login`으로 로그인
                                2. 응답으로 받은 `accessToken`을 Authorization 헤더에 추가
                                3. 형식: `Authorization: Bearer {accessToken}`
                                
                                ### 기본 계정
                                - 관리자: admin / admin123
                                - 사용자: user / user123
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Backend Development Team")
                                .email("backend@example.com")
                                .url("https://github.com/example/jwt-auth"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development Server"),
                        new Server().url("https://api.example.com").description("Production Server")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .name("Authorization")
                                        .description("JWT 토큰을 입력하세요. 형식: Bearer {your-jwt-token}")));
    }
}
