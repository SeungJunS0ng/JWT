> 운영 환경에서 사용하기 전에 보안 검토를 수행하시기 바랍니다.
# JWT Authentication System

> Spring Boot 3.3.5 기반 완전한 JWT 인증 시스템

## 프로젝트 개요

이 프로젝트는 Spring Boot를 기반으로 한 완전한 JWT(JSON Web Token) 인증 및 권한 관리 시스템입니다. 
현대적인 웹 애플리케이션에서 필요한 모든 보안 기능을 제공합니다.

## 주요 기능

### 🔐 인증 & 보안
- JWT Access Token & Refresh Token 기반 인증
- 역할 기반 접근 제어 (RBAC): ADMIN, MODERATOR, USER
- Rate Limiting으로 브루트포스 공격 방지
- 로그인 시도 추적 및 의심스러운 활동 탐지
- 비밀번호 암호화 (BCrypt)
- CSRF 보호 및 CORS 설정

### 👤 사용자 관리
- 사용자 회원가입 및 프로필 관리
- 비밀번호 변경 및 계정 상태 관리
- 다중 기기 로그인 지원 (최대 5개 활성 세션)
- 사용자 검색 및 필터링

### 🛡️ 관리자 기능
- 전체 사용자 관리 (생성, 수정, 삭제, 활성화/비활성화)
- 시스템 모니터링 및 헬스체크
- 로그인 기록 및 보안 이벤트 추적
- 사용자별 활성 세션 관리

### 📊 모니터링 & 로깅
- Spring Boot Actuator를 통한 헬스체크
- 상세한 로그인 시도 기록
- 보안 이벤트 추적
- 성능 모니터링

## 기술 스택

- **Backend**: Spring Boot 3.3.5, Spring Security 6, Spring Data JPA
- **Database**: H2 (개발), MySQL (운영)
- **Authentication**: JWT (JJWT 0.11.0)
- **Documentation**: Swagger/OpenAPI 3
- **Build Tool**: Gradle
- **Container**: Docker
- **Testing**: JUnit 5, Spring Boot Test

## 시작하기

### 필요 조건

- Java 17 이상
- Gradle 7.0 이상
- Docker (선택사항)

### 로컬 환경 설정

1. **프로젝트 클론**
```bash
git clone <repository-url>
cd jwt-auth-demo
```

2. **애플리케이션 실행**
```bash
# Gradle 사용
./gradlew bootRun

# 또는 JAR 파일 빌드 후 실행
./gradlew build
java -jar build/libs/demo-0.0.1-SNAPSHOT.jar
```

3. **애플리케이션 접속**
- 메인 애플리케이션: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Database Console: http://localhost:8080/h2-console

### Docker 실행

```bash
# Docker 이미지 빌드
docker build -t jwt-auth-demo .

# 컨테이너 실행
docker run -p 8080:8080 jwt-auth-demo
```

## API 문서

### 기본 계정

개발 및 테스트를 위해 다음 계정들이 자동으로 생성됩니다:

- **관리자**: `admin` / `admin123`
- **일반 사용자**: `user` / `user123`

### 주요 엔드포인트

#### 🔑 인증 API (`/api/auth`)

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| POST | `/login` | 로그인 | Public |
| POST | `/register` | 회원가입 | Public |
| POST | `/refresh` | 토큰 갱신 | Public |
| POST | `/logout` | 로그아웃 | Authenticated |
| POST | `/logout-all` | 전체 기기 로그아웃 | Authenticated |

#### 👤 사용자 API (`/api/user`)

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | `/profile` | 내 프로필 조회 | User |
| PUT | `/profile` | 프로필 수정 | User |
| POST | `/change-password` | 비밀번호 변경 | User |
| GET | `/active-sessions` | 활성 세션 조회 | User |

#### 🛡️ 관리자 API (`/api/admin`)

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | `/users` | 사용자 목록 조회 | Admin |
| POST | `/users` | 사용자 생성 | Admin |
| PUT | `/users/{username}` | 사용자 수정 | Admin |
| DELETE | `/users/{username}` | 사용자 삭제 | Admin |
| GET | `/login-history` | 로그인 기록 조회 | Admin |
| GET | `/security-events` | 보안 이벤트 조회 | Admin |

### 인증 방법

1. **로그인**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

2. **토큰 사용**
```bash
curl -X GET http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## 프로젝트 구조

```
src/main/java/com/jwtauth/
├── config/              # 설정 클래스들
│   ├── SecurityConfig.java
│   ├── SwaggerConfig.java
│   ├── JpaConfig.java
│   └── ...
├── controller/          # REST 컨트롤러들
│   ├── AuthController.java
│   ├── UserController.java
│   ├── AdminController.java
│   └── PublicController.java
├── dto/                 # 데이터 전송 객체들
│   ├── request/
│   └── response/
├── entity/              # JPA 엔티티들
│   ├── User.java
│   ├── RefreshToken.java
│   └── LoginAttempt.java
├── repository/          # 데이터 저장소 인터페이스들
├── security/            # 보안 관련 클래스들
│   ├── JwtTokenUtil.java
│   ├── JwtAuthenticationFilter.java
│   └── CustomUserDetailsService.java
├── service/             # 비즈니스 로직 서비스들
└── exception/           # 예외 처리 클래스들
```

## 설정

### 애플리케이션 프로파일

- `development`: 개발 환경 (H2 Database)
- `production`: 운영 환경 (MySQL)

### 주요 설정 파일

- `application.yml`: 메인 설정
- `application-test.yml`: 테스트 설정

### 환경 변수

| 변수명 | 설명 | 기본값 |
|--------|------|--------|
| `JWT_SECRET` | JWT 서명 키 | (자동 생성) |
| `DB_URL` | 데이터베이스 URL | jdbc:h2:mem:jwt |
| `DB_USERNAME` | 데이터베이스 사용자명 | sa |
| `DB_PASSWORD` | 데이터베이스 비밀번호 | (없음) |

## 보안 고려사항

### JWT 토큰 관리
- Access Token: 15분 만료
- Refresh Token: 7일 만료
- 사용자당 최대 5개 활성 세션
- 토큰 무효화 및 블랙리스트 관리

### Rate Limiting
- IP당 분당 최대 10회 로그인 시도
- 15분 임시 잠금 정책
- 의심스러운 IP 자동 탐지

### 데이터 보호
- 비밀번호 BCrypt 암호화
- 민감한 정보 로그 제외
- SQL Injection 방지
- XSS 보호

## 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 통합 테스트만 실행
./gradlew integrationTest
```

## 배포

### Docker 배포

```dockerfile
# 프로덕션 환경에서 MySQL 사용
docker run -d \
  --name jwt-auth-app \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=production \
  -e DB_URL=jdbc:mysql://mysql:3306/jwtauth \
  -e DB_USERNAME=jwtauth \
  -e DB_PASSWORD=your_password \
  jwt-auth-demo
```

### JAR 배포

```bash
java -jar demo-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=production \
  --spring.datasource.url=jdbc:mysql://localhost:3306/jwtauth
```

## 모니터링

### Health Check
- URL: `/actuator/health`
- 데이터베이스 연결 상태, 디스크 사용량 등 확인

### 메트릭스
- URL: `/actuator/info`
- 애플리케이션 정보 및 빌드 정보

## 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.

## 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 연락처

- 프로젝트 링크: [GitHub Repository]
- 이슈 신고: [GitHub Issues]
- 문서: [Swagger UI](http://localhost:8080/swagger-ui.html)

---

> **주의**: 이 프로젝트는 학습 및 데모 목적으로 작성되었습니다.