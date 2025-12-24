package com.oms.orderservice.enums;

public enum OrderStatus {
    PENDING,
    VALIDATING,
    FRAUD_CHECK,
    EXECUTING,
    FILLED,
    REJECTED,
    FAILED,
    CANCELLED
}