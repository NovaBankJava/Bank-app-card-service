package org.example.bankappcardservice.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LuhnTest {

    @Test
    @DisplayName("accepts a valid number")
    void acceptsValidNumber() {
        assertThat(Luhn.isValid("79927398713")).isTrue();
    }

    @Test
    @DisplayName("rejects a number with a wrong check digit")
    void rejectsInvalidNumber() {
        assertThat(Luhn.isValid("79927398714")).isFalse();
    }

    @Test
    @DisplayName("computes the correct check digit")
    void computesCheckDigit() {
        assertThat(Luhn.checkDigit("7992739871")).isEqualTo(3);
    }

    @Test
    @DisplayName("payload plus computed check digit is always valid")
    void payloadPlusCheckDigitIsValid() {
        String payload = "453201511283036";
        int check = Luhn.checkDigit(payload);
        assertThat(Luhn.isValid(payload + check)).isTrue();
    }
}