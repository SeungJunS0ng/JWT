# JWT Authentication System - 운영 가이드

## 빠른 시작

### 1. 로컬 개발 환경 (H2 Database)
```bash
./gradlew bootRun
```
- 애플리케이션: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console

### 2. Docker Compose로 전체 시스템 실행 (MySQL + Redis)
```bash
docker-compose up -d
```

### 3. Docker만으로 실행
```bash
# 이미지 빌드
docker build -t jwt-auth-demo .

# 컨테이너 실행
docker run -p 8080:8080 jwt-auth-demo
```

## API 테스트 예시

### 1. 관리자 로그인
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

### 2. 사용자 회원가입
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123",
    "confirmPassword": "password123",
    "email": "test@example.com",
    "role": "USER"
  }'
```

### 3. 인증이 필요한 API 호출
```bash
curl -X GET http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### 4. 토큰 갱신
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "YOUR_REFRESH_TOKEN"}'
```

## 주요 설정 값

### JWT 설정
- Access Token 만료: 15분
- Refresh Token 만료: 7일
- 최대 활성 세션: 5개

### Rate Limiting
- 분당 최대 로그인 시도: 10회
- 임시 잠금 시간: 15분

### 보안 설정
- 비밀번호 암호화: BCrypt
- CORS: 개발 환경에서만 허용
- CSRF: REST API에서 비활성화

## 환경별 설정

### 개발 환경 (development)
```yaml
spring:
  profiles:
    active: development
  datasource:
    url: jdbc:h2:mem:jwt
    driver-class-name: org.h2.Driver
```

### 운영 환경 (production)
```yaml
spring:
  profiles:
    active: production
  datasource:
    url: jdbc:mysql://localhost:3306/jwtauth
    username: jwtauth
    password: ${DB_PASSWORD}
```

## 모니터링

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### 애플리케이션 정보
```bash
curl http://localhost:8080/actuator/info
```

## 문제 해결

### 1. 포트 충돌
```bash
# 8080 포트를 사용하는 프로세스 확인
netstat -ano | findstr :8080
# 또는
lsof -i :8080

# 다른 포트로 실행
java -jar app.jar --server.port=8081
```

### 2. 메모리 부족
```bash
# JVM 힙 메모리 조정
java -Xmx1g -Xms512m -jar app.jar
```

### 3. 데이터베이스 연결 오류
- MySQL 서비스 상태 확인
- 연결 정보 (URL, 사용자명, 비밀번호) 확인
- 네트워크 연결 상태 확인

## 배포 체크리스트

### 운영 환경 배포 전
- [ ] 데이터베이스 연결 정보 확인
- [ ] JWT 시크릿 키 설정 (환경변수)
- [ ] CORS 설정 확인
- [ ] 로그 레벨 설정 (INFO 이상)
- [ ] SSL/HTTPS 설정
- [ ] 방화벽 규칙 확인
- [ ] 백업 정책 수립

### 성능 최적화
- [ ] JVM 튜닝 옵션 설정
- [ ] 데이터베이스 인덱스 최적화
- [ ] 커넥션 풀 설정
- [ ] 캐시 설정 (Redis)

## 보안 권장사항

1. **JWT 시크릿 키**: 충분히 긴 랜덤 문자열 사용
2. **HTTPS**: 운영 환경에서 반드시 사용
3. **비밀번호 정책**: 최소 8자, 대소문자+숫자+특수문자
4. **로그 관리**: 민감한 정보 로그 제외
5. **정기 업데이트**: 의존성 라이브러리 보안 패치

## 성능 모니터링

### 주요 메트릭
- 응답 시간
- 처리량 (TPS)
- 메모리 사용률
- CPU 사용률
- 데이터베이스 연결 수

### 로그 분석
```bash
# 로그인 실패 분석
grep "BadCredentialsException" logs/jwt-auth.log

# 응답 시간 분석
grep "took" logs/jwt-auth.log | awk '{print $NF}' | sort -n
```
