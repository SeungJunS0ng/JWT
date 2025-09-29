# JWT Authentication System

Spring Boot 기반의 완전한 JWT 인증 시스템입니다. 사용자 인증, 권한 관리, 토큰 관리 기능을 제공합니다.

## 주요 기능

### 인증 및 권한 관리
- JWT 기반 인증 시스템
- Access Token + Refresh Token 구조
- 역할 기반 접근 제어 (RBAC)
- 사용자 계정 상태 관리 (활성화/비활성화/잠금)

### 사용자 관리
- 사용자 등록 및 프로필 관리
- 비밀번호 변경 및 보안 관리
- 관리자 전용 사용자 관리 기능
- 다중 기기 로그인 관리

### 보안 기능
- 패스워드 암호화 (BCrypt)
- 토큰 만료 및 갱신 관리
- IP 주소 기반 세션 추적
- 비활성 사용자 자동 관리

## 기술 스택

- **Framework**: Spring Boot 3.3.5
- **Security**: Spring Security 6.x
- **Database**: H2 (개발용), MySQL (운영용)
- **ORM**: JPA/Hibernate
- **Authentication**: JWT (JSON Web Token)
- **Documentation**: Swagger/OpenAPI 3
- **Build Tool**: Gradle

## 프로젝트 구조

```
src/main/java/com/jwtauth/
├── config/                 # 설정 클래스
│   ├── SecurityConfig.java
│   ├── SwaggerConfig.java
│   ├── WebConfig.java
│   └── JpaConfig.java
├── controller/             # REST API 컨트롤러
│   ├── AuthController.java
│   ├── UserController.java
│   └── AdminController.java
├── dto/                    # 데이터 전송 객체
│   ├── request/
│   └── response/
├── entity/                 # JPA 엔티티
│   ├── User.java
│   └── RefreshToken.java
├── repository/             # 데이터 접근 계층
│   ├── UserRepository.java
│   └── RefreshTokenRepository.java
├── security/               # 보안 관련 클래스
│   ├── JwtTokenUtil.java
│   ├── JwtProperties.java
│   ├── JwtAuthenticationFilter.java
│   └── CustomUserDetailsService.java
├── service/                # 비즈니스 로직
│   ├── AuthService.java
│   ├── UserService.java
│   └── RefreshTokenService.java
├── exception/              # 예외 처리
│   └── GlobalExceptionHandler.java
└── JwtAuthApplication.java # 메인 애플리케이션
```

## 설치 및 실행

### 요구사항
- Java 17 이상
- Gradle 8.x

### 로컬 실행
```bash
# 프로젝트 클론
git clone <repository-url>
cd jwt-auth-system

# 의존성 설치 및 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun
```

### 설정 파일
`src/main/resources/application.yml`에서 다음 항목들을 환경에 맞게 수정하세요:

```yaml
spring:
  jwt:
    secret: "your-secret-key-here"
    expirationTime: 900000      # 15분
    refreshTokenExpirationTime: 604800000  # 7일
  
  datasource:
    url: jdbc:mysql://localhost:3306/jwtauth
    username: your-username
    password: your-password
```

## API 문서

애플리케이션 실행 후 다음 URL에서 API 문서를 확인할 수 있습니다:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI 스펙: `http://localhost:8080/v3/api-docs`

## 주요 API 엔드포인트

### 인증 API (`/api/auth`)
- `POST /login` - 사용자 로그인
- `POST /signup` - 회원가입
- `POST /refresh` - 토큰 갱신
- `POST /logout` - 로그아웃
- `POST /logout-all` - 모든 기기에서 로그아웃
- `POST /change-password` - 비밀번호 변경

### 사용자 API (`/api/user`)
- `GET /profile` - 내 프로필 조회
- `PUT /profile` - 프로필 수정
- `GET /tokens` - 내 토큰 목록
- `GET /dashboard` - 사용자 대시보드

### 관리자 API (`/api/admin`)
- `GET /dashboard` - 관리자 대시보드
- `GET /users` - 전체 사용자 목록
- `GET /users/role/{role}` - 역할별 사용자 조회
- `PUT /users/{username}` - 사용자 정보 수정
- `DELETE /users/{username}` - 사용자 삭제
- `GET /stats` - 시스템 통계

## 기본 계정

개발 및 테스트를 위한 기본 계정이 자동으로 생성됩니다:

| 사용자명 | 비밀번호 | 역할 |
|---------|---------|------|
| admin   | admin123| ADMIN|
| user    | user123 | USER |

## 사용법 예시

### 로그인 및 토큰 받기
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user",
    "password": "user123"
  }'
```

### 인증이 필요한 API 호출
```bash
curl -X GET http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer your-jwt-token"
```

### 토큰 갱신
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Authorization: Bearer your-refresh-token"
```

## 보안 고려사항

### JWT 토큰 보안
- 강력한 시크릿 키 사용 (최소 512비트)
- 적절한 토큰 만료 시간 설정
- HTTPS 사용 권장
- 토큰을 로컬 스토리지 대신 HTTP-Only 쿠키에 저장 권장

### 데이터베이스 보안
- 비밀번호 BCrypt 해싱
- 민감한 정보 암호화
- 정기적인 백업 및 복구 계획

## 모니터링 및 로깅

### 로그 레벨 설정
```yaml
logging:
  level:
    com.jwtauth: INFO
    org.springframework.security: DEBUG
```

### 주요 로그 항목
- 로그인/로그아웃 이벤트
- 토큰 갱신 이벤트
- 보안 관련 이벤트
- 에러 및 예외 상황

## 테스트

```bash
# 단위 테스트 실행
./gradlew test

# 통합 테스트 실행
./gradlew integrationTest

# 테스트 리포트 확인
open build/reports/tests/test/index.html
```

## 배포

### 운영 환경 설정
1. `application-prod.yml` 파일 생성
2. 데이터베이스 설정 변경
3. JWT 시크릿 키 변경
4. HTTPS 설정
5. 로깅 레벨 조정

### Docker 배포
```dockerfile
FROM openjdk:17-jre-slim
COPY build/libs/demo-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 문제 해결

### 자주 발생하는 문제
1. **토큰 만료**: 토큰 만료 시간 확인 및 리프레시 토큰 사용
2. **권한 오류**: 사용자 역할 및 권한 설정 확인
3. **데이터베이스 연결**: 데이터베이스 URL 및 인증 정보 확인

### 디버깅 팁
- 로그 레벨을 DEBUG로 설정하여 상세 정보 확인
- H2 콘솔을 통한 데이터베이스 상태 확인
- Swagger UI를 통한 API 테스트

## 기여 방법

1. 이슈 등록
2. 브랜치 생성
3. 코드 작성 및 테스트
4. Pull Request 제출

## 라이선스

MIT License

## 문의

프로젝트 관련 문의사항은 이슈를 통해 남겨주세요.
