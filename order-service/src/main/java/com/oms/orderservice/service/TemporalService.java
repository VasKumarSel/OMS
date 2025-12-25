package com.oms.orderservice.service;

import com.oms.orderservice.config.TemporalConfig;
import com.oms.orderservice.temporal.workflow.TradeOrderWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemporalService {

    private final WorkflowClient workflowClient;
    private final TemporalConfig temporalConfig;

    public String startOrderProcessingWorkflow(Long orderId) {
        log.info("Starting Temporal workflow for order ID: {}", orderId);

        try {
            // Create workflow options
            WorkflowOptions options = WorkflowOptions.newBuilder()
                    .setWorkflowId("order-workflow-" + orderId)
                    .setTaskQueue(temporalConfig.getTaskQueue())
                    .setWorkflowExecutionTimeout(Duration.ofMinutes(5)) // 5 minutes total timeout
                    .setWorkflowTaskTimeout(Duration.ofSeconds(10))
                    .build();

            // Create workflow stub
            TradeOrderWorkflow workflow = workflowClient.newWorkflowStub(
                    TradeOrderWorkflow.class,
                    options
            );

            // Start workflow asynchronously
            WorkflowClient.start(workflow::processOrder, orderId);

            String workflowId = "order-workflow-" + orderId;
            log.info("Temporal workflow started with ID: {}", workflowId);

            return workflowId;
        } catch (Exception e) {
            log.error("Failed to start Temporal workflow for order {}: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Failed to start order processing workflow", e);
        }
    }
}
