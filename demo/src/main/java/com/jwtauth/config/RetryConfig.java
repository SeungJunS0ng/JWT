package com.jwtauth.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class RetryConfig {
    // Spring Retry 의존성이 없으므로 기본 설정만 유지
    // 필요시 build.gradle에 spring-retry 의존성 추가 가능
}
