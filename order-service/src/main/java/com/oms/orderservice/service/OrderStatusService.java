package com.oms.orderservice.service;

import com.oms.orderservice.entity.Order;
import com.oms.orderservice.entity.OrderHistory;
import com.oms.orderservice.enums.OrderStatus;
import com.oms.orderservice.repository.OrderHistoryRepository;
import com.oms.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderStatusService {

    private final OrderRepository orderRepository;
    private final OrderHistoryRepository orderHistoryRepository;


    @Transactional
    public void updateOrderExecution(Long orderId, Integer filledQuantity, java.math.BigDecimal executionPrice, java.math.BigDecimal fees) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        order.setFilledQuantity(filledQuantity);
        order.setExecutionPrice(executionPrice);
        order.setFees(fees);

        // Update status based on filled quantity
        if (filledQuantity.equals(order.getQuantity())) {
            order.setStatus(OrderStatus.FILLED);
            addOrderHistory(orderId, OrderStatus.FILLED, "Order fully executed");
        } else if (filledQuantity > 0) {
            order.setStatus(OrderStatus.FILLED);
            addOrderHistory(orderId, OrderStatus.FILLED, "Order partially executed");
        }

        orderRepository.save(order);
        log.info("Order {} execution updated: filled={}, price={}, fees={}", orderId, filledQuantity, executionPrice, fees);
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus newStatus, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        orderRepository.save(order);

        // Add history entry
        addOrderHistory(orderId, newStatus, reason);

        log.info("Order {} status updated from {} to {}: {}", orderId, oldStatus, newStatus, reason);
    }

    private void addOrderHistory(Long orderId, OrderStatus status, String reason) {
        OrderHistory history = new OrderHistory(orderId, status, reason);
        orderHistoryRepository.save(history);
    }
}
