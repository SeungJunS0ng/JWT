package com.demo.service;

import com.demo.dto.JoinDTO;
import com.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class JoinService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // 사용자가 입력한 비밀번호를 암호화하여 안전하게 저장

    public JoinService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void joinProcess(JoinDTO joinDTO) {
        // JoinDTO를 받아서, 사용자가 입력한 **아이디(username)**가 이미 존재하는지 확인
        // 만약 존재하면 예외를 던지고, 존재하지 않으면 비밀번호를 암호화한 후, UserEntity를 만들어 저장
        if (userRepository.existsByUsername(joinDTO.getUsername())) { // 사용자가 입력한 username이 이미 데이터베이스에 존재하는지 확인
            throw new IllegalStateException("Username already exists");
        }

        // JoinDTO에서 toEntity 메서드를 호출하여 UserEntity로 변환 후 저장
        userRepository.save(joinDTO.toEntity(passwordEncoder));
        /* JoinDTO에서 UserEntity로 변환하는 메서드로, DTO에서 **passwordEncoder**를
         사용하여 비밀번호를 암호화한 후 **UserEntity**로 변환 */
    }
}