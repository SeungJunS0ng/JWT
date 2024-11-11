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

// Spring Security의 보안 설정
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
                /* SRF(Cross-Site Request Forgery)는 웹 애플리케이션에 대한 공격을 방지하는 기능.
                 하지만 API 서버에서는 보통 CSRF를 사용할 필요가 없기 때문에 비활성화. */
                .csrf().disable()

                // HTTP 요청에 대해 권한을 설정하는 부분
                .authorizeHttpRequests()

                // login과 /signup 경로는 누구나 접근할 수 있도록 허용합니다. 즉, 인증 없이도 이 경로들은 접근 가능합니다.
                .requestMatchers("/login", "/signup").permitAll()

                // 그 외의 모든 경로는 인증된 사용자만 접근할 수 있도록 설정
                .anyRequest().authenticated()  // 나머지 요청은 인증 필요
                .and()

                // JWT 인증을 위한 필터인 jwtFilter를 기존의 UsernamePasswordAuthenticationFilter 필터 전에 추가
                // 이 필터는 모든 요청에 대해 JWT 토큰을 검증하고 인증을 수행
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
