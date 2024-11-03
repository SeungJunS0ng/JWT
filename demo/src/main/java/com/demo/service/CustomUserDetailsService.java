package com.demo.service;

import com.demo.dto.CustomUserDetails;
import com.demo.entity.UserEntity;
import com.demo.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository; // 사용자 리포지토리 인스턴스

    // 생성자를 통해 UserRepository 주입
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository; // 인스턴스 변수 초기화
    }

    // 사용자 이름으로 사용자 세부 정보 로드
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // DB에서 사용자 정보 조회
        UserEntity userData = userRepository.findByUsername(username);

        if (userData != null) {
            // UserDetails에 담아서 반환하면 AuthenticationManager가 검증함
            return new CustomUserDetails(userData);
        }

        // 사용자 이름이 존재하지 않을 경우 예외를 던짐
        throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
    }
}

