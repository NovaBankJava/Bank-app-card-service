package org.example.bankappcardservice.infra.adapter.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard response envelope. Every endpoint replies with HTTP 200; "
        + "the outcome is signaled by the `status` field.")
public record ApiResponse<T>(

        @Schema(description = "Result code: 0 = success, 1 = invalid input, "
                + "3 = card generation failed", example = "0")
        int status,

        @Schema(description = "Human-readable description of the result", example = "OK")
        String description,

        @Schema(description = "Response payload, present on success and null otherwise")
        T data) {

    public static final int SUCCESS = 0;
    public static final int INVALID_INPUT = 1;
    public static final int GENERATION_FAILED = 3;
    public static final int UNEXPECTED_ERROR = 99;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(SUCCESS, "OK", data);
    }

    public static <T> ApiResponse<T> ok() {
        return new ApiResponse<>(SUCCESS, "OK", null);
    }

    public static <T> ApiResponse<T> error(int status, String description) {
        return new ApiResponse<>(status, description, null);
    }
}