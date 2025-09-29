package com.jwtauth.controller;

import com.jwtauth.dto.request.*;
import com.jwtauth.dto.response.*;
import com.jwtauth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "Authentication", description = "인증 관련 API - 로그인, 회원가입, 토큰 관리")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    @Operation(
            summary = "사용자 로그인",
            description = "사용자명/이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "로그인 정보",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(
                                    name = "로그인 예시",
                                    value = "{\n  \"username\": \"user\",\n  \"password\": \"user123\"\n}"
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TokenResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 - 잘못된 사용자명 또는 비밀번호",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 형식",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @RequestBody @Validated LoginRequest loginRequest,
            HttpServletRequest request
    ) {
        log.info("로그인 시도: {}", loginRequest.getUsername());

        TokenResponse tokenResponse = authService.login(loginRequest, request);

        log.info("로그인 성공: {}", loginRequest.getUsername());
        return ResponseEntity.ok(ApiResponse.success("로그인 성공", tokenResponse));
    }

    @PostMapping("/signup")
    @Operation(
            summary = "회원가입",
            description = "새로운 사용자 계정을 생성합니다. 사용자명과 이메일은 고유해야 합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "회원가입 정보",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserRegistrationRequest.class),
                            examples = @ExampleObject(
                                    name = "회원가입 예시",
                                    value = "{\n  \"username\": \"newuser\",\n  \"password\": \"password123\",\n  \"confirmPassword\": \"password123\",\n  \"email\": \"newuser@example.com\",\n  \"role\": \"USER\"\n}"
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "회원가입 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 - 유효성 검사 실패"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "충돌 - 이미 존재하는 사용자명 또는 이메일"
            )
    })
    public ResponseEntity<ApiResponse<Void>> signup(@RequestBody @Validated UserRegistrationRequest request) {
        log.info("회원가입 요청: {}", request.getUsername());

        authService.register(request);

        log.info("회원가입 성공: {}", request.getUsername());
        return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다.", null));
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "토큰 갱신",
            description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "토큰 갱신 성공",
                    content = @Content(schema = @Schema(implementation = TokenResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "유효하지 않은 리프레시 토큰"
            )
    })
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
            @Parameter(description = "Bearer 리프레시 토큰", required = true)
            @RequestHeader("Authorization") String refreshToken,
            HttpServletRequest request
    ) {
        log.info("토큰 갱신 요청");

        if (refreshToken.startsWith("Bearer ")) {
            refreshToken = refreshToken.substring(7);
        }

        TokenResponse tokenResponse = authService.refreshToken(refreshToken, request);

        log.info("토큰 갱신 성공");
        return ResponseEntity.ok(ApiResponse.success("토큰 갱신 성공", tokenResponse));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "로그아웃",
            description = "현재 로그인한 사용자의 세션을 종료하고 토큰을 무효화합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 요청"
            )
    })
    public ResponseEntity<ApiResponse<Void>> logout(
            @Parameter(description = "Bearer 액세스 토큰", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "사용자명", required = true)
            @RequestParam String username
    ) {
        log.info("로그아웃 요청");

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        authService.logout(token, username);

        log.info("로그아웃 성공");
        return ResponseEntity.ok(ApiResponse.success("로그아웃 성공", null));
    }

    @PostMapping("/logout-all")
    @Operation(
            summary = "모든 기기에서 로그아웃",
            description = "사용자의 모든 기기에서 로그아웃하여 모든 토큰을 무효화합니다."
    )
    public ResponseEntity<ApiResponse<Void>> logoutAll(
            @Parameter(description = "사용자명", required = true)
            @RequestParam String username
    ) {
        authService.logoutAll(username);
        return ResponseEntity.ok(ApiResponse.success("모든 기기에서 로그아웃 성공", null));
    }

    @PostMapping("/change-password")
    @Operation(
            summary = "비밀번호 변경",
            description = "현재 비밀번호로 인증한 후 새 비밀번호로 변경합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "비밀번호 변경 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 - 비밀번호 불일치"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "현재 비밀번호 인증 실패"
            )
    })
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Parameter(description = "사용자명", required = true)
            @RequestParam String username,
            @RequestBody @Validated PasswordChangeRequest request
    ) {
        authService.changePassword(username, request);
        return ResponseEntity.ok(ApiResponse.success("비밀번호 변경 성공", null));
    }
}
