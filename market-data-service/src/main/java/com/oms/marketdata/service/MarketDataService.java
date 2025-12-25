package com.oms.marketdata.service;

import com.oms.marketdata.dto.MarketPriceDto;
import com.oms.marketdata.dto.MarketStatusDto;
import com.oms.marketdata.entity.MarketPrice;
import com.oms.marketdata.repository.MarketPriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketDataService {

    private final MarketPriceRepository marketPriceRepository;
    private final Random random = new Random();

    // EST Market hours: 9:30 AM - 4:00 PM
    private static final LocalTime MARKET_OPEN = LocalTime.of(9, 30);
    private static final LocalTime MARKET_CLOSE = LocalTime.of(16, 0);
    private static final ZoneId EST_ZONE = ZoneId.of("America/New_York");

    public MarketPriceDto getPrice(String symbol) {
        MarketPrice marketPrice = marketPriceRepository.findBySymbol(symbol.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Symbol not found: " + symbol));

        return new MarketPriceDto(
                marketPrice.getSymbol(),
                marketPrice.getBidPrice(),
                marketPrice.getAskPrice(),
                marketPrice.getLastPrice(),
                marketPrice.getVolume(),
                marketPrice.getUpdatedAt()
        );
    }

    public MarketStatusDto getMarketStatus() {
        ZonedDateTime currentEst = ZonedDateTime.now(EST_ZONE);
        LocalDateTime currentTime = currentEst.toLocalDateTime();
        LocalTime currentTimeOnly = currentTime.toLocalTime();
        System.out.println(currentTime+"  df "+currentTimeOnly+" df  "+currentEst);
        DayOfWeek currentDay = currentTime.getDayOfWeek();

        // Market is closed on weekends
        boolean isWeekday = currentDay != DayOfWeek.SATURDAY && currentDay != DayOfWeek.SUNDAY;
        boolean isMarketHours = currentTimeOnly.isAfter(MARKET_OPEN) && currentTimeOnly.isBefore(MARKET_CLOSE);
        boolean isOpen = isWeekday && isMarketHours;

        String message;
        if (!isWeekday) {
            message = "Market is closed - Weekend";
        } else if (!isMarketHours) {
            if (currentTimeOnly.isBefore(MARKET_OPEN)) {
                message = "Market is closed - Before trading hours";
            } else {
                message = "Market is closed - After trading hours";
            }
        } else {
            message = "Market is open";
        }

        LocalDateTime marketOpenTime = currentTime.toLocalDate().atTime(MARKET_OPEN);
        LocalDateTime marketCloseTime = currentTime.toLocalDate().atTime(MARKET_CLOSE);

        return new MarketStatusDto(isOpen, message, currentTime, marketOpenTime, marketCloseTime, EST_ZONE.getId());
    }

    public boolean isValidSymbol(String symbol) {
        return marketPriceRepository.existsBySymbol(symbol.toUpperCase());
    }
}
