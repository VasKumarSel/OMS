package com.oms.orderservice.temporal.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface TradeOrderWorkflow {

    @WorkflowMethod
    void processOrder(Long orderId);
}
