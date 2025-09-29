package com.jwtauth.controller;

import com.jwtauth.dto.request.UserSearchRequest;
import com.jwtauth.dto.request.UserUpdateRequest;
import com.jwtauth.dto.response.*;
import com.jwtauth.entity.User;
import com.jwtauth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Management", description = "관리자 전용 API - 사용자 관리, 시스템 관리")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminController {

    private final UserService userService;

    @GetMapping("/dashboard")
    @Operation(
            summary = "관리자 대시보드",
            description = "관리자 대시보드 정보 및 시스템 통계를 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "대시보드 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = Map.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "접근 권한 없음 - 관리자 권한 필요"
            )
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> adminDashboard() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Map<String, Object> dashboardData = new HashMap<>();
        dashboardData.put("username", username);
        dashboardData.put("message", "관리자 대시보드에 접근하셨습니다.");
        dashboardData.put("access_level", "ADMIN");
        dashboardData.put("total_users", userService.getAllUsers().size());
        dashboardData.put("active_users", userService.getActiveUserCount());
        dashboardData.put("admin_count", userService.getAdminCount());
        dashboardData.put("user_count", userService.getUsersByRole(User.Role.USER).size());
        dashboardData.put("moderator_count", userService.getUsersByRole(User.Role.MODERATOR).size());

        log.info("관리자 대시보드 접근: {}", username);
        return ResponseEntity.ok(ApiResponse.success("관리자 대시보드", dashboardData));
    }

    @GetMapping("/users")
    @Operation(
            summary = "전체 사용자 목록 조회",
            description = "검색 조건에 따라 전체 사용자 목록을 페이지네이션으로 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "사용자 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @ModelAttribute UserSearchRequest searchRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        PageResponse<UserResponse> users = userService.searchUsers(searchRequest);

        log.info("전체 사용자 목록 조회 by 관리자: {} (페이지: {}, 크기: {})",
                username, searchRequest.getPage(), searchRequest.getSize());
        return ResponseEntity.ok(ApiResponse.success("전체 사용자 목록", users));
    }

    @GetMapping("/users/role/{role}")
    @Operation(
            summary = "역할별 사용자 조회",
            description = "특정 역할을 가진 사용자들을 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "역할별 사용자 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 역할"
            )
    })
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(
            @Parameter(description = "사용자 역할 (USER, ADMIN, MODERATOR)", required = true)
            @PathVariable String role,
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "20") int size) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        try {
            User.Role userRole = User.Role.valueOf(role.toUpperCase());
            PageResponse<UserResponse> users = userService.getUsersByRole(userRole, page, size);

            log.info("역할별({}) 사용자 목록 조회 by 관리자: {}", role, username);
            return ResponseEntity.ok(ApiResponse.success(role + " 역할 사용자 목록", users.getContent()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("유효하지 않은 역할입니다: " + role));
        }
    }

    @PutMapping("/users/{targetUsername}")
    @Operation(
            summary = "사용자 정보 수정",
            description = "특정 사용자의 정보를 수정합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "사용자 정보 수정 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음"
            )
    })
    public ResponseEntity<ApiResponse<Void>> updateUser(
            @Parameter(description = "수정할 사용자명", required = true)
            @PathVariable String targetUsername,
            @RequestBody @Validated UserUpdateRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String adminUsername = authentication.getName();

        userService.updateUser(targetUsername, request);

        log.info("사용자 '{}' 정보 수정 by 관리자: {}", targetUsername, adminUsername);
        return ResponseEntity.ok(ApiResponse.success("사용자 정보가 수정되었습니다.", null));
    }

    @DeleteMapping("/users/{targetUsername}")
    @Operation(
            summary = "사용자 삭제",
            description = "특정 사용자를 시스템에서 완전히 삭제합니다."
    )
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @Parameter(description = "삭제할 사용자명", required = true)
            @PathVariable String targetUsername) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String adminUsername = authentication.getName();

        if (adminUsername.equals(targetUsername)) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("자기 자신은 삭제할 수 없습니다."));
        }

        userService.deleteUser(targetUsername);

        log.info("사용자 '{}' 삭제 by 관리자: {}", targetUsername, adminUsername);
        return ResponseEntity.ok(ApiResponse.success("사용자가 삭제되었습니다.", null));
    }

    @PutMapping("/users/{targetUsername}/enable")
    @Operation(
            summary = "사용자 활성화",
            description = "특정 사용자의 계정을 활성화합니다."
    )
    public ResponseEntity<ApiResponse<Void>> enableUser(@PathVariable String targetUsername) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String adminUsername = authentication.getName();

        userService.enableUser(targetUsername);

        log.info("사용자 '{}' 활성화 by 관리자: {}", targetUsername, adminUsername);
        return ResponseEntity.ok(ApiResponse.success("사용자 계정이 활성화되었습니다.", null));
    }

    @PutMapping("/users/{targetUsername}/disable")
    @Operation(
            summary = "사용자 비활성화",
            description = "특정 사용자의 계정을 비활성화합니다."
    )
    public ResponseEntity<ApiResponse<Void>> disableUser(@PathVariable String targetUsername) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String adminUsername = authentication.getName();

        if (adminUsername.equals(targetUsername)) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("자기 자신은 비활성화할 수 없습니다."));
        }

        userService.disableUser(targetUsername);

        log.info("사용자 '{}' 비활성화 by 관리자: {}", targetUsername, adminUsername);
        return ResponseEntity.ok(ApiResponse.success("사용자 계정이 비활성화되었습니다.", null));
    }

    @PutMapping("/users/{targetUsername}/lock")
    @Operation(
            summary = "사용자 잠금",
            description = "특정 사용자의 계정을 잠급니다."
    )
    public ResponseEntity<ApiResponse<Void>> lockUser(@PathVariable String targetUsername) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String adminUsername = authentication.getName();

        if (adminUsername.equals(targetUsername)) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("자기 자신은 잠글 수 없습니다."));
        }

        userService.lockUser(targetUsername);

        log.info("사용자 '{}' 잠금 by 관리자: {}", targetUsername, adminUsername);
        return ResponseEntity.ok(ApiResponse.success("사용자 계정이 잠겼습니다.", null));
    }

    @PutMapping("/users/{targetUsername}/unlock")
    @Operation(
            summary = "사용자 잠금 해제",
            description = "특정 사용자의 계정 잠금을 해제합니다."
    )
    public ResponseEntity<ApiResponse<Void>> unlockUser(@PathVariable String targetUsername) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String adminUsername = authentication.getName();

        userService.unlockUser(targetUsername);

        log.info("사용자 '{}' 잠금 해제 by 관리자: {}", targetUsername, adminUsername);
        return ResponseEntity.ok(ApiResponse.success("사용자 계정 잠금이 해제되었습니다.", null));
    }

    @GetMapping("/stats")
    @Operation(
            summary = "시스템 통계",
            description = "전체 시스템의 사용자 통계 정보를 조회합니다."
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Map<String, Object> stats = new HashMap<>();
        long totalUsers = userService.getAllUsers().size();
        long adminUsers = userService.getUsersByRole(User.Role.ADMIN).size();
        long regularUsers = userService.getUsersByRole(User.Role.USER).size();
        long moderatorUsers = userService.getUsersByRole(User.Role.MODERATOR).size();
        long activeUsers = userService.getActiveUserCount();

        stats.put("total_users", totalUsers);
        stats.put("active_users", activeUsers);
        stats.put("admin_users", adminUsers);
        stats.put("regular_users", regularUsers);
        stats.put("moderator_users", moderatorUsers);
        stats.put("inactive_users", totalUsers - activeUsers);
        stats.put("admin_percentage", totalUsers > 0 ? Math.round((double) adminUsers / totalUsers * 100) : 0);

        log.info("시스템 통계 조회 by 관리자: {}", username);
        return ResponseEntity.ok(ApiResponse.success("시스템 통계", stats));
    }

    @GetMapping("/users/inactive")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getInactiveUsers(
            @RequestParam(defaultValue = "30") int days) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        List<UserResponse> inactiveUsers = userService.getInactiveUsers(days);

        log.info("비활성 사용자 목록 조회 by 관리자: {} ({}일 이상)", username, days);
        return ResponseEntity.ok(ApiResponse.success(days + "일 이상 비활성 사용자 목록", inactiveUsers));
    }

    @PostMapping("/users/disable-inactive")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> disableInactiveUsers(
            @RequestParam(defaultValue = "90") int days) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        int disabledCount = userService.disableInactiveUsers(days);

        Map<String, Integer> result = new HashMap<>();
        result.put("disabled_count", disabledCount);

        log.info("비활성 사용자 비활성화 실행 by 관리자: {} ({}일 이상, {} 명 처리)", username, days, disabledCount);
        return ResponseEntity.ok(ApiResponse.success("비활성 사용자 비활성화 완료", result));
    }
}
