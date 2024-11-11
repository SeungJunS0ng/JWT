package com.demo.controller;

import com.demo.jwt.JWTUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/token")
public class TokenReissueController {

    private final JWTUtil jwtUtil;

    @Value("${spring.jwt.secret}")
    private String secret;

    public TokenReissueController(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissueToken(@RequestHeader("Authorization") String oldToken) {
        if (oldToken == null || !oldToken.startsWith("Bearer ")) {
            return ResponseEntity.status(400).body("Invalid token format");
        }

        String token = oldToken.split(" ")[1];

        if (jwtUtil.isExpired(token)) {
            return ResponseEntity.status(400).body("Token is expired");
        }

        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);

        String newToken = jwtUtil.createJwt(username, role, 60 * 60 * 10L); // 새로 발급된 토큰

        return ResponseEntity.ok(newToken);
    }
}
