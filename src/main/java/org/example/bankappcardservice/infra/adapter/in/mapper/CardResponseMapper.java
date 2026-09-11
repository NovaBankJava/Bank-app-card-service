package org.example.bankappcardservice.infra.adapter.in.mapper;

import org.example.bankappcardservice.domain.model.Card;
import org.example.bankappcardservice.infra.adapter.in.dto.CardResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.YearMonth;

@Mapper(componentModel = "spring")
public interface CardResponseMapper {

    @Mapping(target = "expiry", expression = "java(format(card.getExpiry()))")
    CardResponse toResponse(Card card);

    default String format(YearMonth expiry) {
        return expiry == null ? null : expiry.toString(); // yyyy-MM
    }
}