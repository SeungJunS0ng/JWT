package com.jwtauth.service;

import com.jwtauth.entity.LoginAttempt;
import com.jwtauth.repository.LoginAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LoginAttemptService {

    private final LoginAttemptRepository loginAttemptRepository;

    @Async
    public CompletableFuture<Void> recordSuccessfulLogin(String username, String ipAddress, String userAgent) {
        LoginAttempt attempt = LoginAttempt.createSuccessful(username, ipAddress, userAgent);
        loginAttemptRepository.save(attempt);
        log.debug("성공적인 로그인 기록: {} from {}", username, ipAddress);
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> recordFailedLogin(String username, String ipAddress, String userAgent, String reason) {
        LoginAttempt attempt = LoginAttempt.createFailed(username, ipAddress, userAgent, reason);
        loginAttemptRepository.save(attempt);
        log.warn("실패한 로그인 기록: {} from {} - {}", username, ipAddress, reason);
        return CompletableFuture.completedFuture(null);
    }

    @Transactional(readOnly = true)
    public List<LoginAttempt> getUserLoginHistory(String username, int limit) {
        List<LoginAttempt> attempts = loginAttemptRepository.findByUsernameOrderByAttemptTimeDesc(username);
        return attempts.stream().limit(limit).toList();
    }

    @Transactional(readOnly = true)
    public boolean isIpSuspicious(String ipAddress) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long failedAttempts = loginAttemptRepository.countFailedAttemptsByIpSince(ipAddress, oneHourAgo);
        return failedAttempts >= 10; // 1시간 내 10회 이상 실패 시 의심스러운 IP로 판단
    }

    @Transactional(readOnly = true)
    public List<LoginAttempt> getSuspiciousActivities(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return loginAttemptRepository.findFailedAttemptsBetween(since, LocalDateTime.now());
    }
}
