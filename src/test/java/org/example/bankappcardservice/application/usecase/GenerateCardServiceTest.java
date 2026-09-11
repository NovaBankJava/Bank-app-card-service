package org.example.bankappcardservice.application.usecase;

import org.example.bankappcardservice.domain.model.Card;
import org.example.bankappcardservice.domain.model.Luhn;
import org.example.bankappcardservice.domain.ports.out.CardRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateCardServiceTest {

    private static final String BIN = "400000";

    @Mock
    private CardRepositoryPort cardRepository;

    private GenerateCardService service;
    private final Clock clock = Clock.fixed(Instant.parse("2026-01-15T00:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        service = new GenerateCardService(cardRepository, new Random(42), clock, BIN);
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    @DisplayName("generates a 16-digit Luhn-valid number starting with the BIN")
    void generatesValidNumber() {
        when(cardRepository.existsByNumber(anyString())).thenReturn(false);

        Card card = service.generate("user-1", "acc-1");

        assertThat(card.getNumber()).hasSize(16).startsWith(BIN);
        assertThat(Luhn.isValid(card.getNumber())).isTrue();
    }

    @Test
    @DisplayName("generates a 3-digit CVV")
    void generatesCvv() {
        when(cardRepository.existsByNumber(anyString())).thenReturn(false);

        Card card = service.generate("user-1", "acc-1");

        assertThat(card.getCvv()).matches("\\d{3}");
    }

    @Test
    @DisplayName("sets expiry four years ahead of the current month")
    void setsExpiry() {
        when(cardRepository.existsByNumber(anyString())).thenReturn(false);

        Card card = service.generate("user-1", "acc-1");

        assertThat(card.getExpiry()).isEqualTo(YearMonth.of(2030, 1));
    }

    @Test
    @DisplayName("links the card to the given user and account")
    void linksUserAndAccount() {
        when(cardRepository.existsByNumber(anyString())).thenReturn(false);

        Card card = service.generate("user-1", "acc-1");

        assertThat(card.getUserId()).isEqualTo("user-1");
        assertThat(card.getAccountId()).isEqualTo("acc-1");
    }

    @Test
    @DisplayName("retries on collision until it finds a unique number")
    void retriesOnCollision() {
        when(cardRepository.existsByNumber(anyString())).thenReturn(true, false);

        Card card = service.generate("user-1", "acc-1");

        assertThat(card.getNumber()).hasSize(16);
        verify(cardRepository, times(2)).existsByNumber(anyString());
    }
}