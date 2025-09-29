-- MySQL 초기화 스크립트
-- JWT Authentication System

-- 데이터베이스 생성 (이미 존재하는 경우 무시)
CREATE DATABASE IF NOT EXISTS jwtauth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 사용자 생성 및 권한 부여
CREATE USER IF NOT EXISTS 'jwtauth'@'%' IDENTIFIED BY 'jwtauth123';
GRANT ALL PRIVILEGES ON jwtauth.* TO 'jwtauth'@'%';
FLUSH PRIVILEGES;

-- 데이터베이스 사용
USE jwtauth;

-- 타임존 설정
SET time_zone = '+09:00';

-- 초기 설정 완료 메시지
SELECT 'JWT Authentication Database initialized successfully!' as message;
