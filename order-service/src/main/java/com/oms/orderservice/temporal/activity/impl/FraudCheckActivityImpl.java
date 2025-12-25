package com.oms.orderservice.temporal.activity.impl;

import com.oms.orderservice.entity.Order;
import com.oms.orderservice.entity.User;
import com.oms.orderservice.enums.OrderStatus;
import com.oms.orderservice.repository.OrderRepository;
import com.oms.orderservice.repository.UserRepository;
import com.oms.orderservice.service.OrderService;
import com.oms.orderservice.service.OrderStatusService;
import com.oms.orderservice.temporal.activity.FraudCheckActivity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class FraudCheckActivityImpl implements FraudCheckActivity {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderStatusService orderService;
    private final Random random = new Random();

    @Override
    public void performFraudCheck(Long orderId) {
        log.info("Starting fraud check for order ID: {}", orderId);

        try {
            // Get the order
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

            // Update status to FRAUD_CHECK
            orderService.updateOrderStatus(orderId, OrderStatus.FRAUD_CHECK, "Starting fraud and compliance check");

            // Simulate processing time (500ms - 2s)
            int delayMs = 500 + random.nextInt(1500);
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Fraud check interrupted", e);
            }

            // Fraud checks
            performSizeCheck(order);
            performUserCheck(order);

            // Simulate random fraud detection (2% chance)
            if (random.nextInt(100) < 2) {
                String reason = "Suspicious trading pattern detected";
                log.warn("Fraud detected for order {}: {}", orderId, reason);
                orderService.updateOrderStatus(orderId, OrderStatus.REJECTED, "Fraud check failed: " + reason);
                throw new RuntimeException("Fraud check failed: " + reason);
            }

            log.info("Fraud check passed for order ID: {}", orderId);

        } catch (Exception e) {
            log.error("Fraud check failed for order ID {}: {}", orderId, e.getMessage());
            if (!e.getMessage().contains("Fraud check failed")) {
                orderService.updateOrderStatus(orderId, OrderStatus.REJECTED, "Fraud check error: " + e.getMessage());
            }
            throw new RuntimeException("Fraud check failed: " + e.getMessage(), e);
        }

    }

    private void performUserCheck(Order order) {
        // Check user status and history
        User user = userRepository.findById(order.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found: " + order.getUserId()));

        if (!user.getIsActive()) {
            throw new RuntimeException("User account is inactive");
        }

        // Check for new users (created less than 24 hours ago)
        LocalDateTime yesterday = LocalDateTime.now().minus(1, ChronoUnit.DAYS);
        if (user.getCreatedAt().isAfter(yesterday)) {
            log.warn("New user detected, applying additional scrutiny");

            // For new users, limit order size
            if (order.getQuantity() > 100) {
                throw new RuntimeException("Order quantity too large for new user");
            }
        }

        log.debug("User check passed for user: {}", user.getUsername());
    }

    private void performSizeCheck(Order order) {
        // Check for unusually large orders
        BigDecimal estimatedValue = calculateEstimatedValue(order);

        if (estimatedValue.compareTo(new BigDecimal("1000000")) > 0) { // $1M limit
            throw new RuntimeException("Order value too large: $" + estimatedValue);
        }

        log.debug("Size check passed: estimated value ${}", estimatedValue);
    }

    private BigDecimal calculateEstimatedValue(Order order) {
        BigDecimal priceEstimate = switch (order.getSymbol()) {
            case "AAPL" -> new BigDecimal("150");
            case "GOOGL" -> new BigDecimal("2750");
            case "MSFT" -> new BigDecimal("380");
            case "TSLA" -> new BigDecimal("245");
            case "AMZN" -> new BigDecimal("155");
            case "META" -> new BigDecimal("350");
            case "NFLX" -> new BigDecimal("485");
            case "NVDA" -> new BigDecimal("875");
            default -> new BigDecimal("100");
        };

        return priceEstimate.multiply(new BigDecimal(order.getQuantity()));
    }
}
