package com.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;
import java.util.Iterator;

// 이 클래스는 메인 페이지 관련 요청을 처리하는 컨트롤러
@Controller
@ResponseBody // 이 클래스의 모든 메서드가 반환하는 값이 HTTP 응답의 본문으로 사용됨
public class MainController {

    // 루트 경로("/")로 GET 요청이 들어오면 이 메서드가 호출됨
    @GetMapping("/")
    public String mainP() {
        // 현재 인증된 사용자의 이름을 가져옴
        String name = SecurityContextHolder.getContext().getAuthentication().getName();

        // 현재 인증 정보를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 사용자의 권한 목록을 가져옴
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        // 권한 목록의 Iterator 생성
        Iterator<? extends GrantedAuthority> iter = authorities.iterator();
        // 첫 번째 권한을 가져옴
        GrantedAuthority auth = iter.next();
        // 권한의 이름을 가져옴
        String role = auth.getAuthority();

        // 메인 컨트롤러 정보를 반환
        return "Main Controller : " + name + ", Role: " + role;
    }
}
