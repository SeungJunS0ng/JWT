package com.jwtauth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulingConfig {
    // 스케줄링 기능을 활성화하여 @Scheduled 어노테이션이 작동하도록 함
}
