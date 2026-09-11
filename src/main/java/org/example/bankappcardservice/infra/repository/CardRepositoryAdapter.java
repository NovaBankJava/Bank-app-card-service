package org.example.bankappcardservice.infra.repository;

import lombok.RequiredArgsConstructor;
import org.example.bankappcardservice.domain.model.Card;
import org.example.bankappcardservice.domain.ports.out.CardRepositoryPort;
import org.example.bankappcardservice.infra.repository.crypto.CardCipher;
import org.example.bankappcardservice.infra.repository.entity.CardEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardRepositoryAdapter implements CardRepositoryPort {

    private final CardJpaRepository jpaRepository;
    private final CardCipher cipher;

    @Override
    public boolean existsByNumber(String number) {
        return jpaRepository.existsByNumberIndex(cipher.blindIndex(number));
    }

    @Override
    public Card save(Card card) {
        String number = card.getNumber();

        CardEntity entity = CardEntity.builder().id(card.getId()).userId(card.getUserId()).accountId(card.getAccountId()).numberEncrypted(cipher.encrypt(number)).numberIndex(cipher.blindIndex(number)).last4(number.substring(number.length() - 4)).expiry(card.getExpiry()).build();

        jpaRepository.save(entity);

        return card;
    }
}