package org.example.bankappcardservice.infra.repository.crypto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CardCipherTest {

    private CardCipher cipher;

    @BeforeEach
    void setUp() {
        // 32-byte base64 keys (test only)
        String aesKey = "LT2iC8h954CxqJtQVPzAwQ5nCgEiFUly+oi16d2Ivco=";
        String hmacKey = "IMnnMcdZdhZ24vd5erR3gZ5JYMTdwT1BDQ+egCMbhZY=";
        cipher = new CardCipher(aesKey, hmacKey);
    }

    @Test
    @DisplayName("encrypt then decrypt returns the original PAN")
    void roundTrip() {
        String pan = "4000001234567899";
        String encrypted = cipher.encrypt(pan);

        assertThat(encrypted).isNotEqualTo(pan);
        assertThat(cipher.decrypt(encrypted)).isEqualTo(pan);
    }

    @Test
    @DisplayName("same plaintext encrypts to different blobs (random IV)")
    void nonDeterministicEncryption() {
        String pan = "4000001234567899";
        assertThat(cipher.encrypt(pan)).isNotEqualTo(cipher.encrypt(pan));
    }

    @Test
    @DisplayName("blind index is deterministic for the same PAN")
    void deterministicBlindIndex() {
        String pan = "4000001234567899";
        assertThat(cipher.blindIndex(pan)).isEqualTo(cipher.blindIndex(pan));
    }

    @Test
    @DisplayName("blind index differs for different PANs")
    void blindIndexDiffersPerPan() {
        assertThat(cipher.blindIndex("4000001234567899"))
                .isNotEqualTo(cipher.blindIndex("4000009876543210"));
    }
}