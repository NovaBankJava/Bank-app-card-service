package org.example.bankappcardservice.domain.ports.in;

import org.example.bankappcardservice.domain.model.Card;

public interface GenerateCardUseCase {

    Card generate(String userId, String accountId);
}