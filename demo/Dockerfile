# Docker를 위한 설정 파일
FROM openjdk:17-jre-slim

# 작업 디렉토리 설정
WORKDIR /app

# JAR 파일 복사
COPY build/libs/demo-0.0.1-SNAPSHOT.jar app.jar

# 포트 노출
EXPOSE 8080

# 환경 변수 설정
ENV SPRING_PROFILES_ACTIVE=production

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
