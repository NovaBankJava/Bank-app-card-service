package org.example.bankappcardservice.domain.ports.out;

import org.example.bankappcardservice.domain.model.Card;

public interface CardRepositoryPort {

    boolean existsByNumber(String number);

    Card save(Card card);
}