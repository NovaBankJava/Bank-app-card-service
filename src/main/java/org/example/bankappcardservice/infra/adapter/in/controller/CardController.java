package org.example.bankappcardservice.infra.adapter.in.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bankappcardservice.domain.model.Card;
import org.example.bankappcardservice.domain.ports.in.GenerateCardUseCase;
import org.example.bankappcardservice.infra.adapter.in.dto.ApiResponse;
import org.example.bankappcardservice.infra.adapter.in.dto.CardResponse;
import org.example.bankappcardservice.infra.adapter.in.dto.GenerateCardRequest;
import org.example.bankappcardservice.infra.adapter.in.mapper.CardResponseMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cards", description = "Virtual card generation")
public class CardController {

    private final GenerateCardUseCase generateCardUseCase;
    private final CardResponseMapper mapper;

    @PostMapping("/generateCard")
    @Operation(
            summary = "Generate a virtual card",
            description = """
                    Generates a Luhn-valid virtual card linked to the given user and \
                    account. The full number and CVV are returned only once, at creation. \
                    Always responds with HTTP 200; the result is signaled by the `status` \
                    field: 0 = success, 1 = invalid input, 3 = card generation failed.""")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Request processed. Check `status` in the body: "
                    + "0 success, 1 invalid input, 3 generation failed.")
    public ApiResponse<CardResponse> generateCard(
            @Valid @RequestBody GenerateCardRequest request) {
        log.info("POST /generateCard for user: {}", request.getUserId());
        Card card = generateCardUseCase.generate(request.getUserId(), request.getAccountId());
        return ApiResponse.ok(mapper.toResponse(card));
    }
}