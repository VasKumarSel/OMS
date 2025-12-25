package com.oms.orderservice.temporal.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface OrderValidationActivity {
    
    @ActivityMethod
    void validateOrder(Long orderId);
    
    @ActivityMethod
    void checkMarketHours();
}