package com.oms.marketdata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketPriceDto {
    private String symbol;
    private BigDecimal bidPrice;
    private BigDecimal askPrice;
    private BigDecimal lastPrice;
    private Long volume;
    private LocalDateTime updatedAt;
}
