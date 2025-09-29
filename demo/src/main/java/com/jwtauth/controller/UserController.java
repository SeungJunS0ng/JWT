package com.jwtauth.controller;

import com.jwtauth.dto.request.PasswordChangeRequest;
import com.jwtauth.dto.request.UserUpdateRequest;
import com.jwtauth.dto.response.ApiResponse;
import com.jwtauth.dto.response.UserProfileResponse;
import com.jwtauth.entity.RefreshToken;
import com.jwtauth.service.AuthService;
import com.jwtauth.service.RefreshTokenService;
import com.jwtauth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "사용자 계정 관리 API - 프로필 조회, 토큰 관리")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @GetMapping("/")
    public ResponseEntity<ApiResponse<Map<String, String>>> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority())
                .orElse("ROLE_USER");

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("username", username);
        userInfo.put("role", role);
        userInfo.put("message", "인증된 사용자입니다.");

        log.info("사용자 정보 조회: {} ({})", username, role);
        return ResponseEntity.ok(ApiResponse.success("사용자 정보 조회 성공", userInfo));
    }

    @GetMapping("/profile")
    @Operation(
            summary = "내 프로필 조회",
            description = "현재 로그인한 사용자의 프로필 정보를 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "프로필 조회 성공",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            )
    })
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        UserProfileResponse profile = userService.getUserProfile(username);

        log.info("사용자 프로필 조회: {}", username);
        return ResponseEntity.ok(ApiResponse.success("내 프로필", profile));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<Void>> updateProfile(@RequestBody @Validated UserUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        userService.updateUser(username, request);

        log.info("사용자 프로필 수정: {}", username);
        return ResponseEntity.ok(ApiResponse.success("프로필 수정 성공", null));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@RequestBody @Validated PasswordChangeRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        authService.changePassword(username, request);

        log.info("사용자 비밀번호 변경: {}", username);
        return ResponseEntity.ok(ApiResponse.success("비밀번호 변경 성공", null));
    }

    @GetMapping("/tokens")
    @Operation(
            summary = "내 토큰 목록 조회",
            description = "현재 사용자의 모든 리프레시 토큰 목록을 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "토큰 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = List.class))
            )
    })
    public ResponseEntity<ApiResponse<List<RefreshToken>>> getMyTokens() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        List<RefreshToken> tokens = refreshTokenService.getUserTokens(username);

        log.info("사용자 토큰 목록 조회: {}", username);
        return ResponseEntity.ok(ApiResponse.success("내 토큰 목록", tokens));
    }

    @PostMapping("/logout-all")
    @Operation(
            summary = "모든 기기에서 로그아웃",
            description = "현재 사용자의 모든 기기에서 로그아웃하여 모든 토큰을 무효화합니다."
    )
    public ResponseEntity<ApiResponse<Void>> logoutAllDevices() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        authService.logoutAll(username);

        log.info("사용자 '{}' 모든 기기에서 로그아웃", username);
        return ResponseEntity.ok(ApiResponse.success("모든 기기에서 로그아웃되었습니다.", null));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<String>> userDashboard() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        log.info("사용자 대시보드 접근: {}", username);
        return ResponseEntity.ok(ApiResponse.success("사용자 대시보드 접근 성공",
            "안녕하세요, " + username + "님! 사용자 대시보드입니다."));
    }
}
