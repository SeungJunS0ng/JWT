package com.jwtauth.controller;

import com.jwtauth.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
@Tag(name = "Public API", description = "인증이 필요하지 않은 공개 API")
public class PublicController {

    @GetMapping("/status")
    @Operation(
            summary = "서비스 상태 확인",
            description = "JWT 인증 서비스의 현재 상태를 확인합니다."
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> getServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "JWT Authentication Service");
        status.put("version", "1.0.0");
        status.put("status", "RUNNING");
        status.put("timestamp", LocalDateTime.now());
        status.put("uptime", "Service is running normally");

        return ResponseEntity.ok(ApiResponse.success("서비스 상태", status));
    }

    @GetMapping("/health")
    @Operation(
            summary = "간단한 헬스체크",
            description = "서비스가 정상적으로 실행 중인지 확인합니다."
    )
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("헬스체크 성공", "OK"));
    }
}
