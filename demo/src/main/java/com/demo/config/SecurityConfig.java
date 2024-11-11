package com.demo.config;

import com.demo.jwt.JWTFilter;
import com.demo.jwt.JWTTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final JWTTokenProvider jwtTokenProvider;

    private final JWTFilter jwtFilter;

    public SecurityConfig(JWTTokenProvider jwtTokenProvider, JWTFilter jwtFilter) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtFilter = jwtFilter;
    }

    // SecurityFilterChain을 사용하여 보안 설정
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()  // CSRF 비활성화 (API 인증에서는 보통 필요하지 않음)
                .authorizeHttpRequests()
                .requestMatchers("/login", "/signup").permitAll()  // 로그인 및 회원가입은 누구나 접근 가능
                .anyRequest().authenticated()  // 나머지 요청은 인증 필요
                .and()
                .addFilterBefore(jwtFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);  // JWT 필터를 인증 필터 전에 추가

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        return authenticationManagerBuilder.build();
    }
}
