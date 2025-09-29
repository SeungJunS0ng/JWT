package com.jwtauth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Slf4j
@Service
public class PasswordValidationService {

    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[@$!%*?&].*");
    private static final Pattern COMMON_PASSWORD_PATTERN = Pattern.compile("(?i).*(password|123456|qwerty|admin|user).*");

    public ValidationResult validatePassword(String password) {
        ValidationResult result = new ValidationResult();

        if (password == null || password.length() < 8) {
            result.addError("비밀번호는 최소 8자 이상이어야 합니다.");
        }

        if (password != null && password.length() > 100) {
            result.addError("비밀번호는 100자를 초과할 수 없습니다.");
        }

        if (!UPPERCASE_PATTERN.matcher(password).matches()) {
            result.addError("대문자를 최소 1개 포함해야 합니다.");
        }

        if (!LOWERCASE_PATTERN.matcher(password).matches()) {
            result.addError("소문자를 최소 1개 포함해야 합니다.");
        }

        if (!DIGIT_PATTERN.matcher(password).matches()) {
            result.addError("숫자를 최소 1개 포함해야 합니다.");
        }

        if (!SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            result.addError("특수문자(@$!%*?&)를 최소 1개 포함해야 합니다.");
        }

        if (COMMON_PASSWORD_PATTERN.matcher(password).matches()) {
            result.addError("일반적인 비밀번호는 사용할 수 없습니다.");
        }

        return result;
    }

    public static class ValidationResult {
        private boolean valid = true;
        private java.util.List<String> errors = new java.util.ArrayList<>();

        public void addError(String error) {
            this.valid = false;
            this.errors.add(error);
        }

        public boolean isValid() {
            return valid;
        }

        public java.util.List<String> getErrors() {
            return errors;
        }

        public String getErrorMessage() {
            return String.join(", ", errors);
        }
    }
}
