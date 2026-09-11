package org.example.bankappcardservice.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.YearMonth;

@Getter
@Builder
public class Card {

    private final String id;
    private final String userId;
    private final String accountId;
    private final String number;
    private final String cvv;
    private final YearMonth expiry;
}