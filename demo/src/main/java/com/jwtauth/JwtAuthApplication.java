package com.jwtauth;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.jwtauth.entity.User;
import com.jwtauth.security.JwtProperties;
import com.jwtauth.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class JwtAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(JwtAuthApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			// 관리자 계정 생성
			if (!userRepository.existsByUsername("admin")) {
				User admin = User.builder()
						.username("admin")
						.password(passwordEncoder.encode("admin123"))
						.email("admin@example.com")
						.role(User.Role.ADMIN)
						.enabled(true)
						.accountNonExpired(true)
						.accountNonLocked(true)
						.credentialsNonExpired(true)
						.build();
				userRepository.save(admin);
				log.info("관리자 계정 생성: admin/admin123");
			}

			// 일반 사용자 계정 생성
			if (!userRepository.existsByUsername("user")) {
				User user = User.builder()
						.username("user")
						.password(passwordEncoder.encode("user123"))
						.email("user@example.com")
						.role(User.Role.USER)
						.enabled(true)
						.accountNonExpired(true)
						.accountNonLocked(true)
						.credentialsNonExpired(true)
						.build();
				userRepository.save(user);
				log.info("사용자 계정 생성: user/user123");
			}
		};
	}
}
