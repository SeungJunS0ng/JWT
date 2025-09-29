package com.jwtauth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "login_attempts", indexes = {
    @Index(name = "idx_login_attempts_ip", columnList = "ip_address"),
    @Index(name = "idx_login_attempts_username", columnList = "username"),
    @Index(name = "idx_login_attempts_timestamp", columnList = "attempt_time")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", length = 50)
    private String username;

    @Column(name = "ip_address", length = 45, nullable = false)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "success", nullable = false)
    private Boolean success;

    @Column(name = "failure_reason", length = 200)
    private String failureReason;

    @CreatedDate
    @Column(name = "attempt_time", nullable = false, updatable = false)
    private LocalDateTime attemptTime;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    public static LoginAttempt createSuccessful(String username, String ipAddress, String userAgent) {
        return LoginAttempt.builder()
                .username(username)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .success(true)
                .build();
    }

    public static LoginAttempt createFailed(String username, String ipAddress, String userAgent, String reason) {
        return LoginAttempt.builder()
                .username(username)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .success(false)
                .failureReason(reason)
                .build();
    }
}
