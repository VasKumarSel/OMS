package com.oms.orderservice.temporal.activity.impl;

import com.oms.orderservice.entity.Order;
import com.oms.orderservice.enums.OrderStatus;
import com.oms.orderservice.repository.OrderRepository;
import com.oms.orderservice.service.OrderService;
import com.oms.orderservice.service.OrderStatusService;
import com.oms.orderservice.service.OrderValidationService;
import com.oms.orderservice.temporal.activity.OrderValidationActivity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderValidationActivityImpl implements OrderValidationActivity {

    private final OrderRepository orderRepository;
    private final OrderStatusService orderService;

    @Qualifier("marketDataWebClient")
    private final WebClient marketDataWebClient;

    @Override
    public void validateOrder(Long orderId) {
        log.info("Starting order validation for order ID: {}", orderId);
        try{
            // Get the order
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
            String upperSymbol = order.getSymbol().toUpperCase();

            orderService.updateOrderStatus(orderId, OrderStatus.VALIDATING, "Starting order validation");

            Boolean isValid = marketDataWebClient
                    .get()
                    .uri("/api/v1/market/validate/{symbol}", upperSymbol)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .timeout(Duration.ofSeconds(3))
                    .block();

            if(!isValid){
                throw new IllegalArgumentException("Invalid symbol: " + order.getSymbol());
            }

            // Validate quantity
            if (order.getQuantity() == null || order.getQuantity() <= 0) {
                throw new IllegalArgumentException("Invalid quantity: " + order.getQuantity());
            }

            if (order.getQuantity() > 1_000_000) {
                throw new IllegalArgumentException("Quantity too large: " + order.getQuantity());
            }

        } catch (Exception e) {
            log.error("Order validation failed for order ID {}: {}", orderId, e.getMessage());
            orderService.updateOrderStatus(orderId, OrderStatus.REJECTED, "Validation failed: " + e.getMessage());
            throw new RuntimeException("Order validation failed: " + e.getMessage(), e);
        }

    }

    @Override
    public void checkMarketHours() {
        log.info("Checking market hours for symbol: {}");

        try {
            Boolean isOpen = marketDataWebClient
                    .get()
                    .uri("/api/v1/market/status/value")
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .timeout(Duration.ofSeconds(3))
                    .block();

            if (Boolean.FALSE.equals(isOpen)) {
                String message = "Market is closed: ";
                log.warn(message);
                throw new IllegalStateException(message);
            }

            log.info("Market is open for trading");

        } catch (Exception e) {
            log.error("Failed to check market hours: {}", e.getMessage());
            throw  new RuntimeException("Market is closed" + e.getMessage(), e);

        }
    }
}
