package com.oms.orderservice.temporal.activity.impl;

import com.oms.orderservice.dto.MarketPriceDto;
import com.oms.orderservice.entity.Order;
import com.oms.orderservice.enums.OrderStatus;
import com.oms.orderservice.enums.OrderType;
import com.oms.orderservice.repository.OrderRepository;
import com.oms.orderservice.service.OrderService;
import com.oms.orderservice.service.OrderStatusService;
import com.oms.orderservice.temporal.activity.OrderExecutionActivity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderExecutionActivityImpl implements OrderExecutionActivity {

    private final OrderRepository orderRepository;
    private final OrderStatusService orderService;
    private final Random random = new Random();

    @Qualifier("marketDataWebClient")
    private final WebClient marketDataWebClient;

    @Value("${app.market-data-service.timeout:3000}")
    private long timeoutMs;

    @Override
    public void executeOrder(Long orderId) {
        log.info("Starting order execution for order ID: {}", orderId);

        try {
            // Get the order
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

            // Update status to EXECUTING
            orderService.updateOrderStatus(orderId, OrderStatus.EXECUTING, "Starting order execution");

            // Get current market price
            MarketPriceDto marketPrice = getPrice(order.getSymbol());

            // Determine execution logic based on Market
            if(order.getOrderType().equals(OrderType.MARKET)){
                ExecutionResult result = executeMarketOrder(order, marketPrice);
                if (result.isExecuted()) {
                    // Calculate fees (0.1% of trade value)
                    BigDecimal tradeValue = result.executionPrice().multiply(new BigDecimal(result.filledQuantity()));
                    BigDecimal fees = tradeValue.multiply(new BigDecimal("0.001")).setScale(4, RoundingMode.HALF_UP);

                    // Update order with execution details
                    orderService.updateOrderExecution(orderId, result.filledQuantity(), result.executionPrice(), fees);

                    log.info("Order {} executed: quantity={}, price={}, fees={}",
                            orderId, result.filledQuantity(), result.executionPrice(), fees);
                } else {
                    // Order was rejected
                    orderService.updateOrderStatus(orderId, OrderStatus.REJECTED, result.rejectionReason());
                    throw new RuntimeException("Order execution failed: " + result.rejectionReason());
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private MarketPriceDto getPrice(String symbol) {
        try {
            log.debug("Getting price for symbol: {}", symbol);
            return marketDataWebClient
                    .get()
                    .uri("/api/v1/market/price/{symbol}", symbol)
                    .retrieve()
                    .bodyToMono(MarketPriceDto.class)
                    .timeout(Duration.ofMillis(timeoutMs))
                    .block();
        } catch (Exception e) {
            log.error("Failed to get price for symbol {}: {}", symbol, e.getMessage());
            throw new RuntimeException("Market data service unavailable for symbol: " + symbol, e);
        }
    }

    private ExecutionResult executeMarketOrder(Order order, MarketPriceDto marketPrice) {
        log.debug("Executing MARKET order for {}", order.getSymbol());

        // Market orders execute at current market price
        BigDecimal executionPrice = switch (order.getSide()) {
            case BUY -> marketPrice.getAskPrice(); // Buy at ask
            case SELL -> marketPrice.getBidPrice(); // Sell at bid
        };

        // Simulate partial fills (5% chance)
        int filledQuantity = order.getQuantity();
        if (random.nextInt(100) < 5) {
            filledQuantity = order.getQuantity() / 2; // Partial fill
            log.warn("Partial fill occurred for order {}: filled {} out of {}",
                    order.getId(), filledQuantity, order.getQuantity());
        }

        return new ExecutionResult(true, filledQuantity, executionPrice, null);
    }

    private record ExecutionResult(
            boolean isExecuted,
            int filledQuantity,
            BigDecimal executionPrice,
            String rejectionReason
    ) {}
}
