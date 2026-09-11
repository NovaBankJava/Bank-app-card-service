package org.example.bankappcardservice.infra.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.YearMonth;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cards")
public class CardEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String accountId;

    @Column(nullable = false, length = 255)
    private String numberEncrypted;

    @Column(nullable = false, unique = true)
    private String numberIndex;

    @Column(nullable = false, length = 4)
    private String last4;

    @Column(nullable = false)
    private YearMonth expiry;

}