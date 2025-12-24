package com.oms.orderservice.dto;

import com.oms.orderservice.enums.OrderSide;
import com.oms.orderservice.enums.OrderType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotBlank(message = "Symbol is required")
    @Size(max = 10, message = "Symbol must be at most 10 characters")
    @Pattern(regexp = "[A-Z]+", message = "Symbol must contain only uppercase letters")
    private String symbol;

    @NotNull(message = "Side is required")
    private OrderSide side;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 1000000, message = "Quantity cannot exceed 1,000,000")
    private Integer quantity;

    @NotNull(message = "Order type is required")
    private OrderType orderType;

    @DecimalMin(value = "0.0001", message = "Limit price must be greater than 0")
    @DecimalMax(value = "999999.9999", message = "Limit price cannot exceed 999,999.9999")
    private BigDecimal limitPrice;

}
