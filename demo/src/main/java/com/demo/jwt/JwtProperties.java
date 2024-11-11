package com.demo.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
@ConfigurationProperties(prefix = "jwt")
// application.properties에서 jwt 접두어로 설정된 값을 자동으로 이 클래스의 필드에 매핑
public class JwtProperties {

    private String secret; // JWT 토큰의 서명에 사용되는 비밀 키, 이 값을 사용해 JWT의 무결성을 검증할 수 있다.
    private long expirationTime; // JWT 토큰의 만료 시간

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }
}