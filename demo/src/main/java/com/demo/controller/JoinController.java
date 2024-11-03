package com.demo.controller;

import com.demo.dto.JoinDTO;
import com.demo.service.JoinService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

// 이 클래스는 사용자 가입 관련 요청을 처리하는 컨트롤러
@Controller
@ResponseBody // 이 클래스의 모든 메서드가 반환하는 값이 HTTP 응답의 본문으로 사용됨
public class JoinController {

    private final JoinService joinService; // 가입 처리를 위한 서비스 클래스의 인스턴스

    // 생성자를 통해 JoinService를 주입받음
    public JoinController(JoinService joinService) {
        this.joinService = joinService;
    }

    // "/join" 경로로 POST 요청이 들어오면 이 메서드가 호출됨
    @PostMapping("/join")
    public String joinProcess(JoinDTO joinDTO) {
        // JoinDTO에서 사용자 이름을 가져와 콘솔에 출력
        System.out.println(joinDTO.getUsername());
        // JoinService의 joinProcess 메서드를 호출하여 가입 처리
        joinService.joinProcess(joinDTO);

        // 성공적으로 처리되었음을 나타내는 문자열 반환
        return "ok";
    }
}