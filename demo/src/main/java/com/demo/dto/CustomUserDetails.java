package com.demo.dto;

import com.demo.entity.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class CustomUserDetails implements UserDetails {

    private final UserEntity userEntity; // 사용자 엔티티 인스턴스

    // 생성자를 통해 UserEntity를 주입받음
    public CustomUserDetails(UserEntity userEntity) {
        this.userEntity = userEntity; // 인스턴스 변수 초기화
    }

    // 사용자 권한을 반환하는 메서드
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>(); // 권한을 담을 컬렉션 생성

        // 사용자의 역할을 권한으로 추가
        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return userEntity.getRole(); // 사용자 역할 반환
            }
        });

        return collection; // 권한 컬렉션 반환
    }

    // 사용자 이름 반환
    @Override
    public String getUsername() {
        return userEntity.getUsername(); // 사용자 이름 반환
    }

    // 비밀번호 반환
    @Override
    public String getPassword() {
        return userEntity.getPassword(); // 비밀번호 반환
    }

    // 계정이 만료되지 않았는지 확인하는 메서드
    @Override
    public boolean isAccountNonExpired() {
        return true; // 만료되지 않았음
    }

    // 계정이 잠기지 않았는지 확인하는 메서드
    @Override
    public boolean isAccountNonLocked() {
        return true; // 잠기지 않았음
    }

    // 자격 증명이 만료되지 않았는지 확인하는 메서드
    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 만료되지 않았음
    }

    // 계정이 활성화되었는지 확인하는 메서드
    @Override
    public boolean isEnabled() {
        return true; // 활성화됨
    }
}
