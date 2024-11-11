package com.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

    @GetMapping("/")
    public String mainP() {
        // 현재 인증된 사용자 정보 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증된 사용자의 이름 가져옴
        String name = authentication.getName();

        // 사용자의 권한(role) 가져옴 (첫 번째 권한만 가져옴)
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority()) // // 권한 값 가져오기
                .orElse("ROLE_USER"); // 만약 권한이 없으면 기본값 "ROLE_USER" 반환

        return "Main Controller : " + name + ", Role: " + role;
    }
}
