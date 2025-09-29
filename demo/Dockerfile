# 멀티 스테이지 빌드를 사용한 최적화된 Dockerfile
FROM eclipse-temurin:17-jdk-alpine AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 래퍼와 빌드 스크립트 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 소스 코드 복사
COPY src src

# 애플리케이션 빌드 (테스트 제외)
RUN chmod +x ./gradlew && ./gradlew build -x test

# 실행 스테이지
FROM eclipse-temurin:17-jre-alpine

# 메타데이터 추가
LABEL maintainer="Backend Development Team"
LABEL version="1.0.0"
LABEL description="JWT Authentication System"

# 비 root 사용자 생성
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 사용자 권한 변경
RUN chown -R appuser:appgroup /app
USER appuser

# 포트 노출
EXPOSE 8080

# 헬스체크 추가
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM 최적화 및 애플리케이션 실행
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UseG1GC", \
    "-XX:+UseStringDeduplication", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]
