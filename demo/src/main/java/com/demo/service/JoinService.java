package com.demo.service;

import com.demo.dto.JoinDTO;
import com.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class JoinService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public JoinService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void joinProcess(JoinDTO joinDTO) {
        if (userRepository.existsByUsername(joinDTO.getUsername())) {
            throw new IllegalStateException("Username already exists");
        }

        // JoinDTO에서 toEntity 메서드를 호출하여 UserEntity로 변환 후 저장
        userRepository.save(joinDTO.toEntity(passwordEncoder));
    }
}