package com.oms.orderservice.service;

import com.oms.orderservice.dto.CreateOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderValidationService {

    @Qualifier("marketDataWebClient")
    private final WebClient marketDataWebClient;

    public void validateOrder(CreateOrderRequest request) {
        validateSymbol(request.getSymbol());
        validateQuantity(request.getQuantity());
        validatePriceConstraints(request);
    }

    private void validateSymbol(String symbol) {
        String upperSymbol = symbol.toUpperCase();

        try {
            Boolean isValid = marketDataWebClient
                    .get()
                    .uri("/api/v1/market/validate/{symbol}", upperSymbol)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .timeout(Duration.ofSeconds(3))
                    .block();
            log.info("Validated the symbol: {} and isValid: {}", upperSymbol, isValid);
            if (!Boolean.TRUE.equals(isValid)) {
                throw new IllegalArgumentException("Invalid symbol: " + symbol);
            }
        } catch (Exception e) {
            log.warn("Failed to validate symbol {} with Market Data Service: {}", symbol, e.getMessage());
            throw new IllegalArgumentException("Unable to validate symbol: " + symbol);
        }
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        if (quantity > 1_000_000) {
            throw new IllegalArgumentException("Quantity cannot exceed 1,000,000 shares");
        }
    }

    private void validatePriceConstraints(CreateOrderRequest request) {
        switch (request.getOrderType()) {
            case LIMIT:
                if (request.getLimitPrice() == null) {
                    throw new IllegalArgumentException("Limit price is required for LIMIT orders");
                }
                if (request.getLimitPrice().doubleValue() <= 0) {
                    throw new IllegalArgumentException("Limit price must be greater than 0");
                }
                break;
            case MARKET:
                if (request.getLimitPrice() != null) {
                    throw new IllegalArgumentException("Limit price should not be specified for MARKET orders");
                }
                break;
        }
    }

    public void validateMarketHours() {
        /*
        try {
            MarketStatusDto status = marketDataWebClient
                    .get()
                    .uri("/api/v1/market/status")
                    .retrieve()
                    .bodyToMono(MarketStatusDto.class)
                    .timeout(Duration.ofSeconds(3))
                    .block();

            if (!status.isOpen()) {
                throw new IllegalArgumentException("Market is closed: " + status.getMessage());
            }
        } catch (Exception e) {
            log.warn("Failed to check market status: {}", e.getMessage());
            // For now, allow orders even if market status check fails
        }
        */
    }
}
