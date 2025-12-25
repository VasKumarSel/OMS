package com.oms.orderservice.temporal.workflow.impl;

import com.oms.orderservice.temporal.activity.FraudCheckActivity;
import com.oms.orderservice.temporal.activity.OrderExecutionActivity;
import com.oms.orderservice.temporal.activity.OrderValidationActivity;
import com.oms.orderservice.temporal.activity.SettlementActivity;
import com.oms.orderservice.temporal.workflow.TradeOrderWorkflow;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Slf4j
public class TradeOrderWorkflowImpl implements TradeOrderWorkflow {

    // Retry policies for all activities
    private static final RetryOptions VALIDATION_RETRY_OPTIONS = RetryOptions.newBuilder()
            .setInitialInterval(Duration.ofSeconds(1))
            .setMaximumInterval(Duration.ofSeconds(10))
            .setBackoffCoefficient(2.0)
            .setMaximumAttempts(3)
            .build();

    private static final RetryOptions FRAUD_CHECK_RETRY_OPTIONS = RetryOptions.newBuilder()
            .setInitialInterval(Duration.ofSeconds(2))
            .setMaximumInterval(Duration.ofSeconds(20))
            .setBackoffCoefficient(2.0)
            .setMaximumAttempts(2)
            .build();

    private static final RetryOptions EXECUTION_RETRY_OPTIONS = RetryOptions.newBuilder()
            .setInitialInterval(Duration.ofSeconds(3))
            .setMaximumInterval(Duration.ofSeconds(30))
            .setBackoffCoefficient(2.0)
            .setMaximumAttempts(5)
            .build();

    private static final RetryOptions NO_RETRY_OPTIONS = RetryOptions.newBuilder()
            .setMaximumAttempts(1)
            .build();

    // Create activity stubs with specific retry policies
    private final OrderValidationActivity validationActivity = Workflow.newActivityStub(
            OrderValidationActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(30))
                    .setRetryOptions(VALIDATION_RETRY_OPTIONS)
                    .build()
    );

    private final FraudCheckActivity fraudCheckActivity = Workflow.newActivityStub(
            FraudCheckActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofMinutes(1))
                    .setRetryOptions(FRAUD_CHECK_RETRY_OPTIONS)
                    .build()
    );

    private final OrderExecutionActivity executionActivity = Workflow.newActivityStub(
            OrderExecutionActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofMinutes(2))
                    .setRetryOptions(EXECUTION_RETRY_OPTIONS)
                    .build()
    );

    private final SettlementActivity settlementActivity = Workflow.newActivityStub(
            SettlementActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofMinutes(1))
                    .setRetryOptions(NO_RETRY_OPTIONS) // Settlement is non-retryable
                    .build()
    );

    @Override
    public void processOrder(Long orderId) {
        log.info("Starting trade order workflow for order ID: {}", orderId);

        try {
            // Step 1: Order Validation
            log.info("Step 1: Validating actitvity {}", orderId);
            validationActivity.validateOrder(orderId);
            validationActivity.checkMarketHours();

            // Step 2: Fraud Check
            log.info("Step 2: Performing fraud check for order {}", orderId);
            fraudCheckActivity.performFraudCheck(orderId);

            // Step 3: Order Execution
            log.info("Step 3: Executing order {}", orderId);
            executionActivity.executeOrder(orderId);

            // Step 4: Settlement (non-retryable, with compensation)
            log.info("Step 4: Settling order {}", orderId);
            try {
                settlementActivity.settleOrder(orderId);
                log.info("Trade order workflow completed successfully for order ID: {}", orderId);
            } catch (Exception settlementException) {
                log.error("Settlement failed for order {}: {}", orderId, settlementException.getMessage());

                // Compensate the order (saga pattern)
                log.info("Compensating order {} due to settlement failure", orderId);
                settlementActivity.compensateOrder(orderId, "Settlement failed: " + settlementException.getMessage());

                throw new RuntimeException("Order processing failed during settlement", settlementException);
            }

        } catch (Exception e) {
            log.error("Trade order workflow failed for order ID {}: {}", orderId, e.getMessage());

            // Perform compensation order
            try {
                log.info("Attempting compensation for failed workflow for order {}", orderId);
                settlementActivity.compensateOrder(orderId, "Workflow failed: " + e.getMessage());
            } catch (Exception compensationException) {
                log.error("Compensation also failed for order {}: {}", orderId, compensationException.getMessage());
            }

            throw new RuntimeException("Order processing workflow failed", e);
        }
    }
}
