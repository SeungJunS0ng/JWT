package com.demo.repository;

import com.demo.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

// UserEntity를 위한 리포지토리 인터페이스
public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    // 사용자 이름으로 존재 여부 확인
    Boolean existsByUsername(String username);

    // 사용자 이름으로 사용자 엔티티 조회
    UserEntity findByUsername(String username);
}