package com.jwtauth.dto.request;

import com.jwtauth.entity.User;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    private User.Role role;
    private Boolean enabled;
    private Boolean accountNonExpired;
    private Boolean accountNonLocked;
}
