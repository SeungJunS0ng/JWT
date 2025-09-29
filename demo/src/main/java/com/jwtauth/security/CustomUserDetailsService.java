package com.jwtauth.security;

import com.jwtauth.entity.User;
import com.jwtauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        log.debug("사용자 정보 로드: {} (역할: {})", user.getUsername(), user.getRole());

        return new CustomUserPrincipal(user);
    }

    @RequiredArgsConstructor
    public static class CustomUserPrincipal implements UserDetails {
        private final User user;

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return Collections.singletonList(new SimpleGrantedAuthority(user.getRole().getAuthority()));
        }

        @Override
        public String getPassword() {
            return user.getPassword();
        }

        @Override
        public String getUsername() {
            return user.getUsername();
        }

        @Override
        public boolean isAccountNonExpired() {
            return user.getAccountNonExpired();
        }

        @Override
        public boolean isAccountNonLocked() {
            return user.getAccountNonLocked();
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return user.getCredentialsNonExpired();
        }

        @Override
        public boolean isEnabled() {
            return user.getEnabled();
        }

        // User 엔티티 반환
        public User getUser() {
            return user;
        }
    }
}
