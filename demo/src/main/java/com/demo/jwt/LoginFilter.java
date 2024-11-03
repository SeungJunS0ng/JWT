package com.demo.jwt;

import com.demo.dto.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication; // 올바른 import
import org.springframework.security.core.AuthenticationException; // 이 import도 추가
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collection;
import java.util.Iterator;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager; // 인증 관리자
    private final JWTUtil jwtUtil; // JWT 유틸리티 클래스

    // 생성자를 통해 의존성 주입
    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
        this.authenticationManager = authenticationManager; // 인증 관리자 초기화
        this.jwtUtil = jwtUtil; // JWT 유틸리티 초기화
    }

    // 인증 시도 메서드
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String username = obtainUsername(request); // 요청에서 사용자 이름 획득
        String password = obtainPassword(request); // 요청에서 비밀번호 획득

        System.out.println(username); // 사용자 이름 출력

        // 인증 토큰 생성
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password, null);
        return authenticationManager.authenticate(authToken); // 인증 시도
    }

    // 인증 성공 시 호출되는 메서드
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {
        // 인증된 사용자 세부 정보 가져오기
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        String username = customUserDetails.getUsername(); // 사용자 이름 가져오기

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities(); // 권한 목록 가져오기
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator(); // 이터레이터 생성
        GrantedAuthority auth = iterator.next(); // 첫 번째 권한 가져오기

        String role = auth.getAuthority(); // 역할 가져오기

        // JWT 생성
        String token = jwtUtil.createJwt(username, role, 60 * 60 * 10L); // 만료 시간 10시간 설정

        // 응답 헤더에 JWT 추가
        response.addHeader("Authorization", "Bearer " + token);
    }

    // 인증 실패 시 호출되는 메서드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        // 로그인 실패 시 401 응답 코드 반환
        response.setStatus(401);
    }
}
