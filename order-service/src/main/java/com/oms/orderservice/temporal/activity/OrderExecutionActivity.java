package com.oms.orderservice.temporal.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface OrderExecutionActivity {

    @ActivityMethod
    void executeOrder(Long orderId);
}
