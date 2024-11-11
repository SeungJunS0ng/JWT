package com.demo.jwt;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JWTUtil {

    @Value("${spring.jwt.secret}") // secret 값을 주입받음
    private String secretKey;

    private final long expirationTime = 1000 * 60 * 60 * 10; // 10 hours

    // JWT 토큰 생성 메서드
    public String createJwt(String username, String role, long expirationTime) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SignatureAlgorithm.HS256, secretKey) // 서명 알고리즘으로 HS256을 사용하고 secretKey로 서명
                .compact();
    }

    // 토큰이 만료되었는지 확인하는 메서드
    public boolean isExpired(String token) {
        try {
            Claims claims = parseClaims(token); // 토큰을 파싱하여 claims 가져오기
            return claims.getExpiration().before(new Date()); // 만료 시간이 현재 시간보다 이전이면 만료된 것으로 판단
        } catch (ExpiredJwtException e) {
            return true; // 만료된 토큰이라면 예외가 발생하고, 만료된 토큰임을 반환
        }
    }

    // 토큰을 파싱하여 claims 정보를 반환하는 메서드
    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey) // 서명 검증을 위한 secretKey 설정
                .build()
                .parseClaimsJws(token)
                .getBody(); // claims의 본문(body)만 반환
    }

    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public String getRole(String token) {
        return (String) parseClaims(token).get("role");
    }
}
