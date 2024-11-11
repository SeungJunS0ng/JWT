package com.demo.dto;

import com.demo.entity.UserEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

public class JoinDTO {

    private String username;
    private String password;

    public JoinDTO() {
    }

    public JoinDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // PasswordEncoder를 사용하여 UserEntity로 변환하는 메서드
    public UserEntity toEntity(PasswordEncoder encoder) {
        return new UserEntity(username, encoder.encode(password), "ROLE_USER");
    }
}