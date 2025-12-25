package com.oms.orderservice.dto;

import com.oms.orderservice.enums.OrderSide;
import com.oms.orderservice.enums.OrderStatus;
import com.oms.orderservice.enums.OrderType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long orderId;
    private Long userId;
    private String symbol;
    private OrderSide side;
    private Integer quantity;
    private OrderType orderType;
    private BigDecimal limitPrice;
    private Integer filledQuantity;
    private BigDecimal executionPrice;
    private BigDecimal fees;
    private OrderStatus status;
    private String workflowId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
