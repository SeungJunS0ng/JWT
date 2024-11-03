package com.demo.service;

import com.demo.dto.JoinDTO;
import com.demo.entity.UserEntity;
import com.demo.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class JoinService {

    private final UserRepository userRepository; // 사용자 리포지토리 인스턴스
    private final BCryptPasswordEncoder bCryptPasswordEncoder; // 비밀번호 인코더 인스턴스

    // 생성자를 통해 UserRepository와 BCryptPasswordEncoder 주입
    public JoinService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository; // 인스턴스 변수 초기화
        this.bCryptPasswordEncoder = bCryptPasswordEncoder; // 인스턴스 변수 초기화
    }

    // 가입 처리 메서드
    public void joinProcess(JoinDTO joinDTO) {
        String username = joinDTO.getUsername(); // 사용자 이름 가져오기
        String password = joinDTO.getPassword(); // 비밀번호 가져오기

        // 사용자 이름 존재 여부 확인
        Boolean isExist = userRepository.existsByUsername(username);

        if (isExist) {
            return; // 이미 존재하면 메서드 종료
        }

        // 새로운 사용자 엔티티 생성
        UserEntity data = new UserEntity();
        data.setUsername(username); // 사용자 이름 설정
        data.setPassword(bCryptPasswordEncoder.encode(password)); // 비밀번호 암호화 후 설정
        data.setRole("ROLE_ADMIN"); // 기본 역할 설정

        // 사용자 엔티티 저장
        userRepository.save(data);
    }
}
