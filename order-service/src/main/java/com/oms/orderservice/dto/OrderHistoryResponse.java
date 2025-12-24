package com.oms.orderservice.dto;

import com.oms.orderservice.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderHistoryResponse {
    private Long id;
    private Long orderId;
    private OrderStatus status;
    private String reason;
    private LocalDateTime changedAt;
}
