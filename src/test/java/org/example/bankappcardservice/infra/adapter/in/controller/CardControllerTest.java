package org.example.bankappcardservice.infra.adapter.in.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.bankappcardservice.domain.model.Card;
import org.example.bankappcardservice.domain.ports.in.GenerateCardUseCase;
import org.example.bankappcardservice.infra.adapter.in.dto.GenerateCardRequest;
import org.example.bankappcardservice.infra.adapter.in.mapper.CardResponseMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.YearMonth;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CardController.class)
@Import(CardControllerTest.MapperConfig.class)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private GenerateCardUseCase useCase;

    @TestConfiguration
    static class MapperConfig {
        @Bean
        GenerateCardUseCase useCase() {
            return Mockito.mock(GenerateCardUseCase.class);
        }

        @Bean
        CardResponseMapper mapper() {
            return org.mapstruct.factory.Mappers.getMapper(CardResponseMapper.class);
        }
    }

    @Test
    void generatesCardAndReturns200WithFullDetails() throws Exception {
        Card card = Card.builder().id("card-1").userId("user-1").accountId("acc-1").number("4000001234567899").cvv("123").expiry(YearMonth.of(2030, 1)).build();
        when(useCase.generate(eq("user-1"), eq("acc-1"))).thenReturn(card);

        GenerateCardRequest req = new GenerateCardRequest();
        req.setUserId("user-1");
        req.setAccountId("acc-1");

        mockMvc.perform(post("/api/v1/cards/generateCard").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req))).andExpect(status().isOk()).andExpect(jsonPath("$.status").value(0)).andExpect(jsonPath("$.data.number").value("4000001234567899")).andExpect(jsonPath("$.data.cvv").value("123")).andExpect(jsonPath("$.data.expiry").value("2030-01"));
    }

    @Test
    void returns200WithStandardBodyOnValidationError() throws Exception {
        mockMvc.perform(post("/api/v1/cards/generateCard").contentType(MediaType.APPLICATION_JSON).content("{}")) // missing userId/accountId
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value(1));
    }
}