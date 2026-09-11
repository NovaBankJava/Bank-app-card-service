package org.example.bankappcardservice.infra.repository;

import org.example.bankappcardservice.infra.repository.entity.CardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardJpaRepository extends JpaRepository<CardEntity, String> {

    boolean existsByNumberIndex(String numberIndex);
}