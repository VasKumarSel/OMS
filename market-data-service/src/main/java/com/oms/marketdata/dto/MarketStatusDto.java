package com.oms.marketdata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketStatusDto {
    private boolean isOpen;
    private String message;
    private LocalDateTime currentTime;
    private LocalDateTime marketOpenTime;
    private LocalDateTime marketCloseTime;
    private String timezone = "America/New_York";
}
