package com.jwtauth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@Configuration
@EnableAsync
public class AuditService {

    @Async
    public CompletableFuture<Void> logSecurityEvent(String eventType, String username, String details, String ipAddress) {
        log.info("SECURITY_EVENT: {} | User: {} | Details: {} | IP: {} | Time: {}",
                eventType, username, details, ipAddress, LocalDateTime.now());

        // 실제 운영에서는 별도 데이터베이스나 로그 시스템에 저장
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> logUserActivity(String activity, String username, String details) {
        log.info("USER_ACTIVITY: {} | User: {} | Details: {} | Time: {}",
                activity, username, details, LocalDateTime.now());

        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> logAdminActivity(String activity, String adminUser, String targetUser, String details) {
        log.info("ADMIN_ACTIVITY: {} | Admin: {} | Target: {} | Details: {} | Time: {}",
                activity, adminUser, targetUser, details, LocalDateTime.now());

        return CompletableFuture.completedFuture(null);
    }
}
