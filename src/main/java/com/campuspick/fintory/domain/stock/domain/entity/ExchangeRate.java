package com.campuspick.fintory.domain.stock.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name="exchange_rates")
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(precision = 12, scale=2)
    private BigDecimal rate;

    @Column(name="rate_date")
    private LocalDateTime rateDate;

}
