package com.oms.marketdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "market_prices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "symbol", nullable = false, unique = true, length = 10)
    private String symbol;

    @Column(name = "bid_price", nullable = false, precision = 10, scale = 4)
    private BigDecimal bidPrice;

    @Column(name = "ask_price", nullable = false, precision = 10, scale = 4)
    private BigDecimal askPrice;

    @Column(name = "last_price", nullable = false, precision = 10, scale = 4)
    private BigDecimal lastPrice;

    @Column(name = "volume")
    private Long volume = 0L;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
