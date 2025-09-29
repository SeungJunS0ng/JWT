package com.jwtauth.service;

import com.jwtauth.dto.request.LoginRequest;
import com.jwtauth.dto.request.PasswordChangeRequest;
import com.jwtauth.dto.request.UserRegistrationRequest;
import com.jwtauth.dto.response.TokenResponse;
import com.jwtauth.entity.RefreshToken;
import com.jwtauth.entity.User;
import com.jwtauth.repository.RefreshTokenRepository;
import com.jwtauth.repository.UserRepository;
import com.jwtauth.security.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final RefreshTokenService refreshTokenService;

    public TokenResponse login(LoginRequest loginRequest, HttpServletRequest request) {
        User user = userRepository.findByUsernameOrEmail(loginRequest.getUsername(), loginRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + loginRequest.getUsername()));

        validateUserAccount(user);

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        // 로그인 시간 업데이트
        user.updateLastLoginTime();
        userRepository.save(user);

        // 기존 활성 토큰들 제한 확인 (최대 5개)
        long activeTokenCount = refreshTokenRepository.countActiveTokensByUsername(user.getUsername());
        if (activeTokenCount >= 5) {
            refreshTokenService.revokeOldestToken(user.getUsername());
        }

        String accessToken = jwtTokenUtil.generateAccessToken(user.getUsername(), user.getRole().getAuthority());
        String refreshToken = jwtTokenUtil.generateRefreshToken(user.getUsername());

        // RefreshToken 저장
        refreshTokenService.saveRefreshToken(refreshToken, user.getUsername(), request);

        log.info("사용자 '{}' 로그인 성공 (IP: {})", user.getUsername(), getClientIpAddress(request));

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtTokenUtil.getExpirationTime())
                .username(user.getUsername())
                .role(user.getRole().name())
                .issuedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusSeconds(jwtTokenUtil.getExpirationTime() / 1000))
                .build();
    }

    public TokenResponse refreshToken(String refreshToken, HttpServletRequest request) {
        if (!jwtTokenUtil.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        if (!jwtTokenUtil.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("리프레시 토큰이 아닙니다.");
        }

        RefreshToken tokenEntity = refreshTokenRepository.findByTokenAndRevokedFalse(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다."));

        if (tokenEntity.isExpired()) {
            throw new IllegalArgumentException("만료된 리프레시 토큰입니다.");
        }

        String username = jwtTokenUtil.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        validateUserAccount(user);

        // 기존 토큰 무효화
        tokenEntity.revoke();
        tokenEntity.updateLastUsed();
        refreshTokenRepository.save(tokenEntity);

        // 새로운 토큰 생성
        String newAccessToken = jwtTokenUtil.generateAccessToken(user.getUsername(), user.getRole().getAuthority());
        String newRefreshToken = jwtTokenUtil.generateRefreshToken(user.getUsername());

        // 새 RefreshToken 저장
        refreshTokenService.saveRefreshToken(newRefreshToken, user.getUsername(), request);

        log.info("사용자 '{}' 토큰 갱신 성공", username);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(jwtTokenUtil.getExpirationTime())
                .username(user.getUsername())
                .role(user.getRole().name())
                .issuedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusSeconds(jwtTokenUtil.getExpirationTime() / 1000))
                .build();
    }

    public void register(UserRegistrationRequest request) {
        validateRegistrationRequest(request);

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 존재하는 사용자명입니다.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(request.getRole())
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        userRepository.save(user);
        log.info("새 사용자 등록 완료: {} (역할: {}, 이메일: {})", user.getUsername(), user.getRole(), user.getEmail());
    }

    public void changePassword(String username, PasswordChangeRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("현재 비밀번호가 일치하지 않습니다.");
        }

        if (!request.isNewPasswordMatching()) {
            throw new IllegalArgumentException("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }

        user.changePassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // 모든 기존 토큰 무효화
        refreshTokenRepository.revokeAllUserTokens(username);

        log.info("사용자 '{}' 비밀번호 변경 완료", username);
    }

    public void logout(String token, String username) {
        if (jwtTokenUtil.validateToken(token) && jwtTokenUtil.isRefreshToken(token)) {
            refreshTokenRepository.findByTokenAndRevokedFalse(token)
                    .ifPresent(refreshToken -> {
                        refreshToken.revoke();
                        refreshTokenRepository.save(refreshToken);
                    });
        }
        log.info("사용자 '{}' 로그아웃", username);
    }

    public void logoutAll(String username) {
        refreshTokenRepository.revokeAllUserTokens(username);
        log.info("사용자 '{}' 모든 기기에서 로그아웃", username);
    }

    private void validateUserAccount(User user) {
        if (!user.getEnabled()) {
            throw new BadCredentialsException("비활성화된 계정입니다.");
        }
        if (!user.getAccountNonLocked()) {
            throw new BadCredentialsException("잠긴 계정입니다.");
        }
        if (!user.getAccountNonExpired()) {
            throw new BadCredentialsException("만료된 계정입니다.");
        }
        if (!user.getCredentialsNonExpired()) {
            throw new BadCredentialsException("비밀번호가 만료되었습니다.");
        }
    }

    private void validateRegistrationRequest(UserRegistrationRequest request) {
        if (!request.isPasswordMatching()) {
            throw new IllegalArgumentException("비밀번호와 확인 비밀번호가 일치하지 않습니다.");
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
