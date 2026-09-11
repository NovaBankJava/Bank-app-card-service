package org.example.bankappcardservice.application.usecase;

import lombok.extern.slf4j.Slf4j;
import org.example.bankappcardservice.domain.exception.CardGenerationException;
import org.example.bankappcardservice.domain.model.Card;
import org.example.bankappcardservice.domain.model.Luhn;
import org.example.bankappcardservice.domain.ports.in.GenerateCardUseCase;
import org.example.bankappcardservice.domain.ports.out.CardRepositoryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.YearMonth;
import java.util.UUID;
import java.util.random.RandomGenerator;

@Slf4j
@Service
public class GenerateCardService implements GenerateCardUseCase {

    private static final int TOTAL_LENGTH = 16;
    private static final int VALIDITY_YEARS = 4;
    private static final int MAX_ATTEMPTS = 5;

    private final CardRepositoryPort cardRepository;
    private final RandomGenerator random;
    private final Clock clock;
    private final String bin;


    public GenerateCardService(CardRepositoryPort cardRepository, RandomGenerator random, Clock clock, @Value("${card.bin}") String bin) {
        this.cardRepository = cardRepository;
        this.random = random;
        this.clock = clock;
        this.bin = bin;
    }

    @Override
    @Transactional
    public Card generate(String userId, String accountId) {
        log.info("Generating virtual card for userId={}, accountId={}", userId, accountId);

        Card card = Card.builder().id(UUID.randomUUID().toString()).userId(userId).accountId(accountId).number(generateUniqueNumber()).cvv(generateCvv()).expiry(YearMonth.now(clock).plusYears(VALIDITY_YEARS)).build();

        Card saved = cardRepository.save(card);
        log.info("Virtual card generated id={} for userId={}", saved.getId(), userId);
        return saved;
    }

    private String generateUniqueNumber() {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            String candidate = generateNumber();
            if (!cardRepository.existsByNumber(candidate)) {
                return candidate;
            }
            log.warn("Card number collision, attempt {}/{}", attempt, MAX_ATTEMPTS);
        }
        throw new CardGenerationException("Could not generate a unique card number after " + MAX_ATTEMPTS + " attempts");
    }

    private String generateNumber() {
        StringBuilder payload = new StringBuilder(bin);
        int bodyLength = TOTAL_LENGTH - 1 - bin.length();
        for (int i = 0; i < bodyLength; i++) {
            payload.append(random.nextInt(10));
        }
        int check = Luhn.checkDigit(payload.toString());
        return payload.append(check).toString();
    }

    private String generateCvv() {
        return String.format("%03d", random.nextInt(1000));
    }
}