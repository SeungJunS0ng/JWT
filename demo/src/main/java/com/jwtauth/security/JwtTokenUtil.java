package com.jwtauth.security;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenUtil {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = new SecretKeySpec(
            jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8),
            SignatureAlgorithm.HS512.getJcaName()
        );
    }

    // Access Token 생성
    public String generateAccessToken(String username, String role) {
        return generateToken(username, role, jwtProperties.getExpirationTime(), TokenType.ACCESS);
    }

    // Refresh Token 생성
    public String generateRefreshToken(String username) {
        return generateToken(username, null, jwtProperties.getRefreshTokenExpirationTime(), TokenType.REFRESH);
    }

    // 공통 토큰 생성 메서드
    private String generateToken(String username, String role, long expirationTime, TokenType tokenType) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationTime);

        JwtBuilder builder = Jwts.builder()
                .setId(UUID.randomUUID().toString()) // JWT ID 추가
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .setIssuer("jwt-auth-service")
                .claim("tokenType", tokenType.name())
                .signWith(secretKey);

        if (role != null) {
            builder.claim("role", role);
        }

        return builder.compact();
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.warn("잘못된 형식의 JWT 토큰: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.warn("잘못된 JWT 서명: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("빈 JWT 토큰: {}", e.getMessage());
            return false;
        }
    }

    // 토큰 타입 확인
    public boolean isAccessToken(String token) {
        try {
            Claims claims = parseToken(token);
            String tokenType = claims.get("tokenType", String.class);
            return TokenType.ACCESS.name().equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            String tokenType = claims.get("tokenType", String.class);
            return TokenType.REFRESH.name().equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }

    // 토큰 만료 확인
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            log.error("토큰 만료 확인 중 오류 발생: {}", e.getMessage());
            return true;
        }
    }

    // 토큰 파싱
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 사용자명 추출
    public String getUsernameFromToken(String token) {
        return parseToken(token).getSubject();
    }

    // 역할 추출
    public String getRoleFromToken(String token) {
        return parseToken(token).get("role", String.class);
    }

    // JWT ID 추출
    public String getJwtIdFromToken(String token) {
        return parseToken(token).getId();
    }

    // 토큰 만료 시간 반환 (밀리초)
    public long getExpirationTime() {
        return jwtProperties.getExpirationTime();
    }

    // 토큰 만료 시간 반환 (초)
    public long getExpirationTimeInSeconds() {
        return jwtProperties.getExpirationTime() / 1000;
    }

    // 토큰 발급 시간 추출
    public Date getIssuedAt(String token) {
        return parseToken(token).getIssuedAt();
    }

    // 토큰 만료 시간 추출
    public Date getExpiration(String token) {
        return parseToken(token).getExpiration();
    }

    // 토큰 남은 시간 (밀리초)
    public long getRemainingTime(String token) {
        try {
            Date expiration = getExpiration(token);
            return Math.max(0, expiration.getTime() - System.currentTimeMillis());
        } catch (Exception e) {
            return 0;
        }
    }

    // 토큰 타입 열거형
    public enum TokenType {
        ACCESS, REFRESH
    }
}
