package com.oms.orderservice.config;

import com.oms.orderservice.temporal.activity.impl.FraudCheckActivityImpl;
import com.oms.orderservice.temporal.activity.impl.OrderExecutionActivityImpl;
import com.oms.orderservice.temporal.activity.impl.OrderValidationActivityImpl;
import com.oms.orderservice.temporal.activity.impl.SettlementActivityImpl;
import com.oms.orderservice.temporal.workflow.impl.TradeOrderWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class TemporalConfig {

    @Value("${app.temporal.server.host:localhost}")
    private String temporalServerHost;

    @Value("${app.temporal.server.port:7233}")
    private int temporalServerPort;

    @Value("${app.temporal.namespace:default}")
    private String temporalNamespace;

    @Value("${app.temporal.task-queue:order-processing}")
    private String taskQueue;

    private final OrderValidationActivityImpl orderValidationActivity;
    private final FraudCheckActivityImpl fraudCheckActivity;
    private final OrderExecutionActivityImpl orderExecutionActivity;
    private final SettlementActivityImpl settlementActivity;
    private WorkerFactory workerFactory;

    @Bean
    public WorkflowServiceStubs workflowServiceStubs() {
        return WorkflowServiceStubs.newServiceStubs(
                WorkflowServiceStubsOptions.newBuilder()
                        .setTarget(temporalServerHost + ":" + temporalServerPort)
                        .build()
        );
    }

    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs serviceStubs) {
        return WorkflowClient.newInstance(
                serviceStubs,
                WorkflowClientOptions.newBuilder()
                        .setNamespace(temporalNamespace)
                        .build()
        );
    }

    @Bean
    public WorkerFactory workerFactory(WorkflowClient workflowClient) {
        this.workerFactory = WorkerFactory.newInstance(workflowClient);
        startWorkers();
        return this.workerFactory;
    }

    public void startWorkers() {
        log.info("Starting Temporal workers...");

        try {
            Worker worker = workerFactory.newWorker(taskQueue);
            // Register workflow implementation
            worker.registerWorkflowImplementationTypes(TradeOrderWorkflowImpl.class);

            // Register activity implementations
            worker.registerActivitiesImplementations(
                    orderValidationActivity,
                    fraudCheckActivity,
                    orderExecutionActivity,
                    settlementActivity
            );

            // Start the worker
            workerFactory.start();

            log.info("Temporal workers started successfully on task queue: {}", taskQueue);
        } catch (Exception e) {
            log.error("Failed to start Temporal workers: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to start Temporal workers", e);
        }
    }

    @PreDestroy
    public void stopWorkers() {
        log.info("Stopping Temporal workers...");

        if (workerFactory != null) {
            workerFactory.shutdown();
            log.info("Temporal workers stopped");
        }
    }

    public String getTaskQueue() {
        return taskQueue;
    }
}
