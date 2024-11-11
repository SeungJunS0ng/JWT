package com.demo.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JWTFilter extends OncePerRequestFilter {

    private final JWTTokenProvider jwtTokenProvider;

    // 생성자 주입으로 JWTTokenProvider를 주입받음
    public JWTFilter(JWTTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // 요청마다 실행되는 필터 메서드
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 요청에서 토큰을 추출
        String token = jwtTokenProvider.resolveToken(request);

        // 토큰이 존재하고 유효한 경우
        if (token != null && jwtTokenProvider.validateToken(token)) {

            // 토큰에서 claims(토큰의 내용)를 추출
            Claims claims = jwtTokenProvider.getClaims(token);

            // 토큰에서 사용자 이름과 역할을 추출
            String username = claims.getSubject(); // subject는 보통 사용자 이름
            String role = claims.get("role", String.class); // 역할을 추출

            // 인증 객체를 만들어서 SecurityContext에 저장
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(username, null, List.of(() -> role))
            );
        }

        // 필터 체인을 계속해서 실행
        filterChain.doFilter(request, response);
    }
}