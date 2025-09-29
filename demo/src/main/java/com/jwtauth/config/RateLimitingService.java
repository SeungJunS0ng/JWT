package com.jwtauth.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RateLimitingService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15;

    private final Map<String, AttemptInfo> loginAttempts = new ConcurrentHashMap<>();

    public boolean isAllowed(String clientIp) {
        AttemptInfo attemptInfo = loginAttempts.get(clientIp);

        if (attemptInfo == null) {
            return true;
        }

        // 잠금 시간이 지났으면 초기화
        if (attemptInfo.isLockoutExpired()) {
            loginAttempts.remove(clientIp);
            return true;
        }

        return attemptInfo.getAttempts() < MAX_ATTEMPTS;
    }

    public void recordFailedAttempt(String clientIp) {
        AttemptInfo attemptInfo = loginAttempts.computeIfAbsent(clientIp,
            k -> new AttemptInfo());

        attemptInfo.incrementAttempts();

        if (attemptInfo.getAttempts() >= MAX_ATTEMPTS) {
            attemptInfo.lockout();
            log.warn("IP {} 잠금 처리됨 - {}번 연속 로그인 실패", clientIp, MAX_ATTEMPTS);
        }
    }

    public void recordSuccessfulAttempt(String clientIp) {
        loginAttempts.remove(clientIp);
    }

    public String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    private static class AttemptInfo {
        private int attempts = 0;
        private LocalDateTime lockoutTime;

        public void incrementAttempts() {
            this.attempts++;
        }

        public void lockout() {
            this.lockoutTime = LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES);
        }

        public boolean isLockoutExpired() {
            return lockoutTime == null || LocalDateTime.now().isAfter(lockoutTime);
        }

        public int getAttempts() {
            return attempts;
        }
    }
}
