package com.demo.jwt;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

public class LoginFilter extends OncePerRequestFilter {

    private final JWTTokenProvider jwtTokenProvider;

    // 생성자에서 JWTTokenProvider를 주입받음
    public LoginFilter(JWTTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // doFilterInternal 메서드에서 실제 필터링 동작을 수행
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = jwtTokenProvider.resolveToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            String username = jwtTokenProvider.getUsernameFromToken(token);

            // UsernamePasswordAuthenticationToken을 생성하여 인증 정보를 설정
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());

            // SecurityContext에 인증 정보를 설정하여, 이후 요청에서 인증을 사용할 수 있게 함
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        // 필터 체인을 계속 실행하여 다음 필터로 넘어가도록 함
        chain.doFilter(request, response);
    }
}