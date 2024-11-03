package com.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // 이 클래스가 Spring 설정 클래스임을 나타냄
public class CorsMvcConfig implements WebMvcConfigurer {

    // CORS 매핑을 추가하는 메서드
    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {
        // 모든 경로에 대해 CORS 허용 설정
        corsRegistry.addMapping("/**")
                // 허용할 출처를 지정
                .allowedOrigins("http://localhost:3000");
    }
}
