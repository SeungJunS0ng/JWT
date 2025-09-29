package com.jwtauth.service;

import com.jwtauth.dto.request.UserSearchRequest;
import com.jwtauth.dto.request.UserUpdateRequest;
import com.jwtauth.dto.response.PageResponse;
import com.jwtauth.dto.response.UserProfileResponse;
import com.jwtauth.dto.response.UserResponse;
import com.jwtauth.entity.User;
import com.jwtauth.repository.RefreshTokenRepository;
import com.jwtauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public UserService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public UserProfileResponse getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        log.info("사용자 프로필 조회: {}", username);

        return UserProfileResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .enabled(user.getEnabled())
                .accountNonExpired(user.getAccountNonExpired())
                .accountNonLocked(user.getAccountNonLocked())
                .credentialsNonExpired(user.getCredentialsNonExpired())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    public PageResponse<UserResponse> searchUsers(UserSearchRequest searchRequest) {
        Pageable pageable = PageRequest.of(
                searchRequest.getPage(),
                searchRequest.getSize(),
                Sort.by(Sort.Direction.fromString(searchRequest.getSortDirection()), searchRequest.getSortBy())
        );

        Specification<User> spec = createUserSpecification(searchRequest);
        Page<User> userPage = userRepository.findAll(spec, pageable);

        List<UserResponse> userResponses = userPage.getContent().stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());

        return PageResponse.<UserResponse>builder()
                .content(userResponses)
                .page(userPage.getNumber())
                .size(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .first(userPage.isFirst())
                .last(userPage.isLast())
                .build();
    }

    public List<UserResponse> getUsersByRole(User.Role role) {
        List<User> users = userRepository.findByRole(role);
        return users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    public PageResponse<UserResponse> getUsersByRole(User.Role role, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> userPage = userRepository.findByRole(role, pageable);

        List<UserResponse> userResponses = userPage.getContent().stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());

        return PageResponse.<UserResponse>builder()
                .content(userResponses)
                .page(userPage.getNumber())
                .size(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .first(userPage.isFirst())
                .last(userPage.isLast())
                .build();
    }

    public long getActiveUserCount() {
        return userRepository.countActiveUsers();
    }

    public long getAdminCount() {
        return userRepository.countByRole(User.Role.ADMIN);
    }

    @Transactional
    public void updateUser(String username, UserUpdateRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        User updatedUser = User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .email(request.getEmail() != null ? request.getEmail() : user.getEmail())
                .role(request.getRole() != null ? request.getRole() : user.getRole())
                .enabled(request.getEnabled() != null ? request.getEnabled() : user.getEnabled())
                .accountNonExpired(request.getAccountNonExpired() != null ? request.getAccountNonExpired() : user.getAccountNonExpired())
                .accountNonLocked(request.getAccountNonLocked() != null ? request.getAccountNonLocked() : user.getAccountNonLocked())
                .credentialsNonExpired(user.getCredentialsNonExpired())
                .lastLoginAt(user.getLastLoginAt())
                .version(user.getVersion())
                .build();

        userRepository.save(updatedUser);
        log.info("사용자 정보 업데이트: {}", username);
    }

    @Transactional
    public void deleteUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        // 관련된 RefreshToken도 모두 삭제
        refreshTokenRepository.revokeAllUserTokens(username);
        userRepository.delete(user);
        log.info("사용자 삭제: {}", username);
    }

    @Transactional
    public void enableUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        user.enableAccount();
        userRepository.save(user);
        log.info("사용자 계정 활성화: {}", username);
    }

    @Transactional
    public void disableUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        user.disableAccount();
        userRepository.save(user);
        // 모든 토큰 무효화
        refreshTokenRepository.revokeAllUserTokens(username);
        log.info("사용자 계정 비활성화: {}", username);
    }

    @Transactional
    public void lockUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        user.lockAccount();
        userRepository.save(user);
        // 모든 토큰 무효화
        refreshTokenRepository.revokeAllUserTokens(username);
        log.info("사용자 계정 잠금: {}", username);
    }

    @Transactional
    public void unlockUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        user.unlockAccount();
        userRepository.save(user);
        log.info("사용자 계정 잠금 해제: {}", username);
    }

    public List<UserResponse> getInactiveUsers(int days) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(days);
        List<User> inactiveUsers = userRepository.findInactiveUsers(threshold);
        return inactiveUsers.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public int disableInactiveUsers(int days) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(days);
        return userRepository.disableInactiveUsers(threshold);
    }

    private UserResponse convertToUserResponse(User user) {
        return UserResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .enabled(user.getEnabled())
                .accountNonExpired(user.getAccountNonExpired())
                .accountNonLocked(user.getAccountNonLocked())
                .credentialsNonExpired(user.getCredentialsNonExpired())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private Specification<User> createUserSpecification(UserSearchRequest searchRequest) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchRequest.getUsername() != null && !searchRequest.getUsername().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("username")),
                        "%" + searchRequest.getUsername().toLowerCase() + "%"
                ));
            }

            if (searchRequest.getEmail() != null && !searchRequest.getEmail().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")),
                        "%" + searchRequest.getEmail().toLowerCase() + "%"
                ));
            }

            if (searchRequest.getRole() != null) {
                predicates.add(criteriaBuilder.equal(root.get("role"), searchRequest.getRole()));
            }

            if (searchRequest.getEnabled() != null) {
                predicates.add(criteriaBuilder.equal(root.get("enabled"), searchRequest.getEnabled()));
            }

            if (searchRequest.getAccountNonLocked() != null) {
                predicates.add(criteriaBuilder.equal(root.get("accountNonLocked"), searchRequest.getAccountNonLocked()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
