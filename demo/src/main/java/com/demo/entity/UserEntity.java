package com.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class UserEntity {

    @Id // 기본 키 필드
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID 자동 생성 전략 설정
    private int id; // 사용자 ID

    private String username; // 사용자 이름

    private String password; // 비밀번호

    private String role; // 사용자 역할
}
