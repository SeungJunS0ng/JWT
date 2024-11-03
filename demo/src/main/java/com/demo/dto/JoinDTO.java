package com.demo.dto;


import lombok.Getter;
import lombok.Setter;

@Setter // 모든 필드에 대한 Setter 메서드 자동 생성
@Getter // 모든 필드에 대한 Getter 메서드 자동 생성
public class JoinDTO {

    private String username; // 사용자 이름
    private String password; // 비밀번호
}