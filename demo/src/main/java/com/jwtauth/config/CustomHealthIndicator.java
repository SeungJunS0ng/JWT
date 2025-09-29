package com.jwtauth.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class CustomHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        // 여기서 애플리케이션의 상태를 확인
        boolean isHealthy = checkApplicationHealth();

        if (isHealthy) {
            return Health.up()
                    .withDetail("status", "JWT Auth Service is running")
                    .withDetail("database", "H2 Database connected")
                    .withDetail("security", "JWT authentication active")
                    .build();
        } else {
            return Health.down()
                    .withDetail("status", "Service unavailable")
                    .build();
        }
    }

    private boolean checkApplicationHealth() {
        // 실제 환경에서는 데이터베이스 연결, 외부 서비스 상태 등을 확인
        return true;
    }
}
