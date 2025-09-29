package com.jwtauth.service;

import com.jwtauth.entity.RefreshToken;
import com.jwtauth.repository.RefreshTokenRepository;
import com.jwtauth.security.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenUtil jwtTokenUtil;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, JwtTokenUtil jwtTokenUtil) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    public void saveRefreshToken(String token, String username, HttpServletRequest request) {
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(7); // 7일 만료

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .username(username)
                .expiryDate(expiryDate)
                .revoked(false)
                .userAgent(request.getHeader("User-Agent"))
                .ipAddress(getClientIpAddress(request))
                .build();

        refreshTokenRepository.save(refreshToken);
        log.debug("RefreshToken 저장: 사용자={}, IP={}", username, refreshToken.getIpAddress());
    }

    public void revokeToken(String token) {
        refreshTokenRepository.findByTokenAndRevokedFalse(token)
                .ifPresent(refreshToken -> {
                    refreshToken.revoke();
                    refreshTokenRepository.save(refreshToken);
                    log.info("RefreshToken 무효화: 사용자={}", refreshToken.getUsername());
                });
    }

    public void revokeOldestToken(String username) {
        List<RefreshToken> activeTokens = refreshTokenRepository.findActiveTokensByUsernameOrderByCreatedAt(username);
        if (!activeTokens.isEmpty()) {
            RefreshToken oldestToken = activeTokens.get(0);
            oldestToken.revoke();
            refreshTokenRepository.save(oldestToken);
            log.info("가장 오래된 RefreshToken 무효화: 사용자={}", username);
        }
    }

    public void revokeAllUserTokens(String username) {
        List<RefreshToken> userTokens = refreshTokenRepository.findByUsernameAndRevokedFalse(username);
        userTokens.forEach(RefreshToken::revoke);
        refreshTokenRepository.saveAll(userTokens);
        log.info("사용자 '{}' 모든 RefreshToken 무효화 ({}개)", username, userTokens.size());
    }

    public List<RefreshToken> getUserTokens(String username) {
        return refreshTokenRepository.findByUsernameOrderByLastUsedAtDesc(username);
    }

    // 매일 자정에 만료된 토큰 정리
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupExpiredTokens() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(1);
        int deletedCount = refreshTokenRepository.deleteExpiredTokens(threshold);
        if (deletedCount > 0) {
            log.info("만료된 RefreshToken {} 개 정리 완료", deletedCount);
        }
    }

    // 30분마다 만료된 토큰 무효화
    @Scheduled(fixedRate = 1800000) // 30분
    public void revokeExpiredTokens() {
        int revokedCount = refreshTokenRepository.revokeExpiredTokens(LocalDateTime.now());
        if (revokedCount > 0) {
            log.info("만료된 RefreshToken {} 개 무효화", revokedCount);
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
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
}
