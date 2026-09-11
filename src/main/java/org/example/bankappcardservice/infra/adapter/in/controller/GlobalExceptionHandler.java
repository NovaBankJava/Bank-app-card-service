package org.example.bankappcardservice.infra.adapter.in.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.bankappcardservice.domain.exception.CardGenerationException;
import org.example.bankappcardservice.infra.adapter.in.dto.ApiResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)gi
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + " " + e.getDefaultMessage())
                .findFirst()
                .orElse("Invalid request");
        log.warn("Validation failed: {}", message);
        return ApiResponse.error(ApiResponse.INVALID_INPUT, message);
    }

    @ExceptionHandler(CardGenerationException.class)
    public ApiResponse<Void> handleGeneration(CardGenerationException ex) {
        log.error("Card generation failed: {}", ex.getMessage());
        return ApiResponse.error(ApiResponse.GENERATION_FAILED, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return ApiResponse.error(ApiResponse.UNEXPECTED_ERROR, "Unexpected error");
    }
}