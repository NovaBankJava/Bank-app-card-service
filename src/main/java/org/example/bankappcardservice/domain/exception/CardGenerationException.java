package org.example.bankappcardservice.domain.exception;

public class CardGenerationException extends RuntimeException {

    public CardGenerationException(String message) {
        super(message);
    }
}