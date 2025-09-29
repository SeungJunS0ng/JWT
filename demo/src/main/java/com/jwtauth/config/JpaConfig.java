package com.jwtauth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaConfig {
    // JPA Auditing을 활성화하여 @CreatedDate, @LastModifiedDate 어노테이션이 작동하도록 함
}
