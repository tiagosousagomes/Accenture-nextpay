package com.nextpay.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldErrorDetail> errors
) {
    public record FieldErrorDetail(String field, String message) {}
}
