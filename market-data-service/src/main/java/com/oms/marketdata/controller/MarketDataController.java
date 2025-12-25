package com.oms.marketdata.controller;

import com.oms.marketdata.dto.MarketPriceDto;
import com.oms.marketdata.dto.MarketStatusDto;
import com.oms.marketdata.service.MarketDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/market")
@RequiredArgsConstructor
@Slf4j
public class MarketDataController {

    private final MarketDataService marketDataService;

    @GetMapping("/price/{symbol}")
    public ResponseEntity<MarketPriceDto> getPrice(@PathVariable String symbol) {
        log.info("Getting price for symbol: {}", symbol);
        try {
            MarketPriceDto price = marketDataService.getPrice(symbol);
            return ResponseEntity.ok(price);
        } catch (IllegalArgumentException e) {
            log.warn("Symbol not found: {}", symbol);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/status")
    public ResponseEntity<MarketStatusDto> getMarketStatus() {
        log.debug("Getting market status");
        MarketStatusDto status = marketDataService.getMarketStatus();
        return ResponseEntity.ok(status);
    }

    @GetMapping("/status/value")
    public ResponseEntity<Boolean> getMarketStatusValue() {
        log.debug("Getting market status value");
        MarketStatusDto status = marketDataService.getMarketStatus();
        return ResponseEntity.ok(status.isOpen());
    }

    @GetMapping("/validate/{symbol}")
    public ResponseEntity<Boolean> validateSymbol(@PathVariable String symbol) {
        log.debug("Validating symbol: {}", symbol);
        boolean isValid = marketDataService.isValidSymbol(symbol);
        return ResponseEntity.ok(isValid);
    }

}
