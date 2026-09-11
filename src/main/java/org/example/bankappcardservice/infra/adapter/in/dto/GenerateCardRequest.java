package org.example.bankappcardservice.infra.adapter.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request to generate a virtual card")
public class GenerateCardRequest {

    @NotBlank
    @Schema(description = "Owner user id", example = "user-123")
    private String userId;

    @NotBlank
    @Schema(description = "Linked account id", example = "acc-456")
    private String accountId;
}