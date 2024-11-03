package com.demo.config;

import com.demo.jwt.JWTFilter;
import com.demo.jwt.JWTUtil;
import com.demo.jwt.LoginFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;


@Configuration // 이 클래스가 Spring 설정 클래스임을 나타냄
@EnableWebSecurity // Spring Security를 활성화함
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration; // 인증 구성을 위한 인스턴스
    private final JWTUtil jwtUtil; // JWT 유틸리티 클래스의 인스턴스

    // 생성자를 통해 의존성 주입
    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration, JWTUtil jwtUtil) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtUtil = jwtUtil;
    }

    // AuthenticationManager 빈 정의
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager(); // 인증 관리자 반환
    }

    // BCryptPasswordEncoder 빈 정의
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder(); // 비밀번호 인코더 반환
    }

    // 보안 필터 체인 설정
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // CORS 설정
        http.cors((corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {
            @Override
            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000")); // 허용할 출처
                configuration.setAllowedMethods(Collections.singletonList("*")); // 허용할 HTTP 메서드
                configuration.setAllowCredentials(true); // 자격 증명 허용
                configuration.setAllowedHeaders(Collections.singletonList("*")); // 허용할 헤더
                configuration.setMaxAge(3600L); // 최대 캐시 시간
                configuration.setExposedHeaders(Collections.singletonList("Authorization")); // 노출할 헤더

                return configuration; // 설정 반환
            }
        })));

        // CSRF 비활성화
        http.csrf((auth) -> auth.disable());

        // 폼 로그인 비활성화
        http.formLogin((auth) -> auth.disable());

        // 기본 인증 비활성화
        http.httpBasic((auth) -> auth.disable());

        // 요청에 대한 권한 설정
        http.authorizeHttpRequests((auth) -> auth
                .requestMatchers("/login", "/", "/join").permitAll() // 로그인, 루트, 가입 경로 허용
                .requestMatchers("/admin").hasRole("ADMIN") // 관리자 경로는 ADMIN 역할만 접근 가능
                .anyRequest().authenticated()); // 그 외의 요청은 인증 필요

        // JWTFilter 등록
        http.addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class);

        // LoginFilter 등록
        http.addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil), UsernamePasswordAuthenticationFilter.class);

        // 세션 관리 설정
        http.sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // 상태 비저장 세션 정책 설정

        return http.build(); // 설정된 보안 필터 체인 반환
    }
}