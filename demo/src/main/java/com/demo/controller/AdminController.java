package com.demo.controller;

// Spring MVC의 Controller 역할을 하는 클래스
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

// 이 클래스는 컨트롤러로 설정됨
@Controller
@ResponseBody // 이 클래스의 모든 메서드가 반환하는 값이 HTTP 응답의 본문으로 사용됨
public class AdminController {

    // "/admin" 경로로 GET 요청이 들어오면 이 메서드가 호출됨
    @GetMapping("/admin")
    public String adminP() {
        // "Admin Controller"라는 문자열을 반환함
        return "Admin Controller";
    }
}
