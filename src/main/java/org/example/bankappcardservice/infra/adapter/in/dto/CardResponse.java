package org.example.bankappcardservice.infra.adapter.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Generated virtual card. Full number and CVV are returned only once, at creation.")
public class CardResponse {

    @Schema(description = "Card id")
    private final String id;

    @Schema(example = "4000001234567899", description = "Full card number (PAN) — shown once")
    private final String number;

    @Schema(example = "123", description = "CVV — shown once, never stored")
    private final String cvv;

    @Schema(example = "2030-01", description = "Expiry (yyyy-MM)")
    private final String expiry;
}