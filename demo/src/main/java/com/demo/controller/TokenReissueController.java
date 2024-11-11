package com.demo.controller;

import com.demo.jwt.JWTUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/token")
public class TokenReissueController {

    private final JWTUtil jwtUtil;

    @Value("${spring.jwt.secret}") // JWT secret 값을 가져옴
    private String secret;

    // 생성자 주입을 통해 JWTUtil을 주입받음
    public TokenReissueController(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissueToken(@RequestHeader("Authorization") String oldToken) {

        // 토큰이 null이거나 "Bearer "로 시작하지 않으면 잘못된 토큰 형식으로 처리
        if (oldToken == null || !oldToken.startsWith("Bearer ")) {
            return ResponseEntity.status(400).body("Invalid token format");
        }

        // "Bearer " 부분을 제외한 실제 토큰 값을 추출
        String token = oldToken.split(" ")[1];

        // 토큰이 만료된 경우
        if (jwtUtil.isExpired(token)) {
            return ResponseEntity.status(400).body("Token is expired");
        }

        // 토큰에서 사용자 이름과 역할을 추출
        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);

        // 새 토큰 발급 (10시간 만료)
        String newToken = jwtUtil.createJwt(username, role, 60 * 60 * 10L); // 새로 발급된 토큰

        // 새 토큰 발급 (10시간 만료)
        return ResponseEntity.ok(newToken);
    }
}
