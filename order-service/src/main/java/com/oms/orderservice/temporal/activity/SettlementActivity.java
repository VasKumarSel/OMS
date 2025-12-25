package com.oms.orderservice.temporal.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface SettlementActivity {
    
    @ActivityMethod
    void settleOrder(Long orderId);
    
    @ActivityMethod
    void compensateOrder(Long orderId, String reason);
}