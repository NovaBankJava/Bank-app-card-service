package org.example.bankappcardservice.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.SecureRandom;
import java.time.Clock;
import java.util.random.RandomGenerator;

@Configuration
public class CardConfig {

    @Bean
    public RandomGenerator randomGenerator() {
        return new SecureRandom();
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}