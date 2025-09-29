package com.jwtauth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String error;
    private String message;
    private String path;
    private int status;
    private LocalDateTime timestamp;

    public static ErrorResponse of(String error, String message, String path, int status) {
        return ErrorResponse.builder()
                .error(error)
                .message(message)
                .path(path)
                .status(status)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
