package com.example.nextgen.domain.service;

import com.example.nextgen.domain.event.WorkflowEvent;
import com.example.nextgen.domain.event.WorkflowExecutionContext;
import com.example.nextgen.domain.orchestration.WorkflowOrchestrator;
import com.example.nextgen.domain.workflow.Workflow;
import com.example.nextgen.domain.workflow.WorkflowId;
import com.example.nextgen.domain.workflow.WorkflowStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 工作流执行器
 * 作为工作流执行的统一入口，负责协调各个组件完成工作流的完整执行生命周期
 *
 * 主要职责：
 * 1. 工作流执行的统一调度和管理
 * 2. 执行状态监控和异常处理
 * 3. 资源管理和并发控制
 * 4. 执行上下文管理
 * 5. 性能监控和日志记录
 * 6. 执行结果收集和回调处理
 */
@Service
public class WorkFlowExecutor {

    Map<WorkflowId,Workflow>workflowRepository =new HashMap<>();

//    private final WorkflowRepository workflowRepository;
    private final WorkflowExecutionEngine executionEngine;
    private final WorkflowOrchestrator defaultOrchestrator;
    private final ApplicationEventPublisher eventPublisher;
//    private final WorkflowPerformanceLogger performanceLogger;
//    private final WorkflowAuditLogger auditLogger;
//    private final WorkflowMonitoringService monitoringService;
//
    // 线程池执行器
    private final Executor workflowExecutor = Executors.newCachedThreadPool();
//    private final Executor agentExecutor;
//    private final Executor eventExecutor;
//
    // 执行上下文管理
    private final Map<WorkflowId, WorkflowExecutionContext> executionContexts = new ConcurrentHashMap<>();
    private final Map<WorkflowId, CompletableFuture<WorkflowExecutionResult>> executionFutures = new ConcurrentHashMap<>();

    // 执行统计
    private final AtomicInteger activeExecutions = new AtomicInteger(0);
    private final AtomicInteger totalExecutions = new AtomicInteger(0);
    private final AtomicInteger successfulExecutions = new AtomicInteger(0);
    private final AtomicInteger failedExecutions = new AtomicInteger(0);

    @Autowired
    public WorkFlowExecutor(
//            WorkflowRepository workflowRepository,
            WorkflowExecutionEngine executionEngine,
            WorkflowOrchestrator defaultOrchestrator,
            ApplicationEventPublisher eventPublisher
//            WorkflowPerformanceLogger performanceLogger,
//            WorkflowAuditLogger auditLogger,
//            WorkflowMonitoringService monitoringService,
//            @Qualifier("workflowExecutor") Executor workflowExecutor,
//            @Qualifier("agentExecutor") Executor agentExecutor,
//            @Qualifier("eventExecutor") Executor eventExecutor
    ) {

//        this.workflowRepository = workflowRepository;
        this.executionEngine = executionEngine;
        this.defaultOrchestrator = defaultOrchestrator;
        this.eventPublisher = eventPublisher;
//        this.performanceLogger = performanceLogger;
//        this.auditLogger = auditLogger;
//        this.monitoringService = monitoringService;
//        this.workflowExecutor = workflowExecutor;
//        this.agentExecutor = agentExecutor;
//        this.eventExecutor = eventExecutor;
    }

    /**
     * 执行工作流
     *
     * @param workflowId 工作流ID
     * @param initialContext 初始上下文数据
     * @param orchestrator 编排器（可选，为null时使用默认编排器）
     * @return 执行结果的Future
     */
//    @WorkflowLog(
//        type = WorkflowLog.LogType.OPERATION,
//        message = "开始执行工作流",
//        logArgs = true,
//        logExecutionTime = true,
//        slowExecutionThreshold = 5000
//    )
    public CompletableFuture<WorkflowExecutionResult> executeWorkflow(
            WorkflowId workflowId,
            Map<String, Object> initialContext,
            WorkflowOrchestrator orchestrator) {

        return CompletableFuture.supplyAsync(() -> {
            WorkflowExecutionContext context = null;
            try {
                // 1. 预检查和准备
                Workflow workflow = prepareWorkflowExecution(workflowId, initialContext);

                // 2. 创建执行上下文
                context = createExecutionContext(workflow);
                executionContexts.put(workflowId, context);

                // 3. 记录执行开始
//                recordExecutionStart(workflow, context);

                // 4. 执行工作流
                WorkflowOrchestrator actualOrchestrator = orchestrator != null ? orchestrator : defaultOrchestrator;
                WorkflowExecutionResult result = doExecuteWorkflow(workflow, context, actualOrchestrator);

                // 5. 记录执行完成
//                recordExecutionComplete(workflow, context, result);

                return result;

            } catch (Exception e) {
                // 6. 处理执行异常
                WorkflowExecutionResult errorResult = handleExecutionError(workflowId, context, e);
//                recordExecutionError(workflowId, context, e);
                return errorResult;

            } finally {
                // 7. 清理资源
                cleanupExecution(workflowId);
            }
        }, workflowExecutor);
    }

    /**
     * 执行工作流（重载方法，使用默认编排器）
     */
    public CompletableFuture<WorkflowExecutionResult> executeWorkflow(
            WorkflowId workflowId,
            Map<String, Object> initialContext) {
        return executeWorkflow(workflowId, initialContext, null);
    }

    /**
     * 执行工作流（重载方法，无初始上下文）
     */
    public CompletableFuture<WorkflowExecutionResult> executeWorkflow(WorkflowId workflowId) {
        return executeWorkflow(workflowId, new HashMap<>(), null);
    }

    /**
     * 批量执行工作流
     */
    // @WorkflowLog(
    //     type = WorkflowLog.LogType.OPERATION,
    //     message = "批量执行工作流",
    //     logArgs = true
    // )
    public CompletableFuture<List<WorkflowExecutionResult>> executeWorkflowsBatch(
            List<WorkflowExecutionRequest> requests) {

        List<CompletableFuture<WorkflowExecutionResult>> futures = requests.stream()
            .map(request -> executeWorkflow(
                request.getWorkflowId(),
                request.getInitialContext(),
                request.getOrchestrator()))
            .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .toList());
    }

    /**
//     * 暂停工作流执行
//     */
//    // @WorkflowLog(
//    //     type = WorkflowLog.LogType.OPERATION,
//    //     message = "暂停工作流执行"
//    // )
//    public CompletableFuture<Boolean> pauseWorkflow(WorkflowId workflowId) {
//        return CompletableFuture.supplyAsync(() -> {
//            try {
//                Optional<Workflow> workflowOpt = workflowRepository.findById(workflowId);
//                if (workflowOpt.isEmpty()) {
//                    return false;
//                }
//
//                Workflow workflow = workflowOpt.get();
//                if (workflow.getStatus() != WorkflowStatus.RUNNING) {
//                    return false;
//                }
//
//                workflow.pause();
//                workflowRepository.save(workflow);
//
//                // 记录审计日志
//                auditLogger.logWorkflowPaused(workflowId, workflow.getName(), "system", "用户请求暂停");
//
//                // 发布事件
//                eventPublisher.publishEvent(new WorkflowEvent.WorkflowPaused(workflowId, LocalDateTime.now()));
//
//                return true;
//
//            } catch (Exception e) {
//                auditLogger.logSecurityEvent(workflowId, "PAUSE_FAILED", "system",
//                    "暂停工作流失败: " + e.getMessage(), "ERROR");
//                return false;
//            }
//        }, workflowExecutor);
//    }
//
//    /**
//     * 恢复工作流执行
//     */
//    // @WorkflowLog(
//    //     type = WorkflowLog.LogType.OPERATION,
//    //     message = "恢复工作流执行"
//    // )
//    public CompletableFuture<Boolean> resumeWorkflow(WorkflowId workflowId) {
//        return CompletableFuture.supplyAsync(() -> {
//            try {
//                Optional<Workflow> workflowOpt = workflowRepository.findById(workflowId);
//                if (workflowOpt.isEmpty()) {
//                    return false;
//                }
//
//                Workflow workflow = workflowOpt.get();
//                if (workflow.getStatus() != WorkflowStatus.PAUSED) {
//                    return false;
//                }
//
//                workflow.resume();
//                workflowRepository.save(workflow);
//
//                // 记录审计日志
//                auditLogger.logWorkflowResumed(workflowId, workflow.getName(), "system", "用户请求恢复");
//
//                // 发布事件
//                eventPublisher.publishEvent(new WorkflowEvent.WorkflowResumed(workflowId, LocalDateTime.now()));
//
//                // 继续执行
//                return continueExecution(workflow);
//
//            } catch (Exception e) {
//                auditLogger.logSecurityEvent(workflowId, "RESUME_FAILED", "system",
//                    "恢复工作流失败: " + e.getMessage(), "ERROR");
//                return false;
//            }
//        }, workflowExecutor);
//    }
//
//    /**
//     * 取消工作流执行
//     */
//    // @WorkflowLog(
//    //     type = WorkflowLog.LogType.OPERATION,
//    //     message = "取消工作流执行"
//    // )
//    public CompletableFuture<Boolean> cancelWorkflow(WorkflowId workflowId, String reason) {
//        return CompletableFuture.supplyAsync(() -> {
//            try {
//                // 取消执行Future
//                CompletableFuture<WorkflowExecutionResult> future = executionFutures.get(workflowId);
//                if (future != null) {
//                    future.cancel(true);
//                }
//
//                // 更新工作流状态
//                Optional<Workflow> workflowOpt = workflowRepository.findById(workflowId);
//                if (workflowOpt.isPresent()) {
//                    Workflow workflow = workflowOpt.get();
//                    workflow.cancel(reason);
//                    workflowRepository.save(workflow);
//
//                    // 记录审计日志
//                    auditLogger.logWorkflowCancelled(workflowId, workflow.getName(), "system", reason);
//
//                    // 发布事件
//                    eventPublisher.publishEvent(new WorkflowEvent.WorkflowCancelled(workflowId, reason, LocalDateTime.now()));
//                }
//
//                // 清理资源
//                cleanupExecution(workflowId);
//
//                return true;
//
//            } catch (Exception e) {
//                auditLogger.logSecurityEvent(workflowId, "CANCEL_FAILED", "system",
//                    "取消工作流失败: " + e.getMessage(), "ERROR");
//                return false;
//            }
//        }, workflowExecutor);
//    }
//
//    /**
//     * 获取工作流执行状态
//     */
//    public WorkflowExecutionStatus getExecutionStatus(WorkflowId workflowId) {
//        WorkflowExecutionContext context = executionContexts.get(workflowId);
//        if (context == null) {
//            return WorkflowExecutionStatus.NOT_FOUND;
//        }
//
//        Optional<Workflow> workflowOpt = workflowRepository.findById(workflowId);
//        if (workflowOpt.isEmpty()) {
//            return WorkflowExecutionStatus.NOT_FOUND;
//        }
//
//        Workflow workflow = workflowOpt.get();
//        return mapWorkflowStatusToExecutionStatus(workflow.getStatus());
//    }

    /**
     * 获取执行统计信息
     */
    public ExecutionStatistics getExecutionStatistics() {
        return ExecutionStatistics.builder()
            .activeExecutions(activeExecutions.get())
            .totalExecutions(totalExecutions.get())
            .successfulExecutions(successfulExecutions.get())
            .failedExecutions(failedExecutions.get())
            .successRate(calculateSuccessRate())
            .build();
    }

    /**
     * 获取所有活跃的执行上下文
     */
    public Map<WorkflowId, WorkflowExecutionContext> getActiveExecutions() {
        return new HashMap<>(executionContexts);
    }

    // ==================== 私有方法 ====================

    /**
     * 准备工作流执行
     */
    private Workflow prepareWorkflowExecution(WorkflowId workflowId, Map<String, Object> initialContext) {
        // 查找工作流
        Optional<Workflow> workflowOpt = Optional.ofNullable(workflowRepository.get(workflowId));
        if (workflowOpt.isEmpty()) {
            throw new IllegalArgumentException("工作流不存在: " + workflowId.getValue());
        }

        Workflow workflow = workflowOpt.get();

        // 检查工作流状态
        if (workflow.getStatus() == WorkflowStatus.RUNNING) {
            throw new IllegalStateException("工作流已在运行中: " + workflowId.getValue());
        }

        // 设置初始上下文
        if (initialContext != null && !initialContext.isEmpty()) {
            workflow.getGlobalContext().putAll(initialContext);
        }

        return workflow;
    }

    /**
     * 创建执行上下文
     */
    private WorkflowExecutionContext createExecutionContext(Workflow workflow) {
        return new WorkflowExecutionContext(
            workflow.getWorkflowId().getValue(),
            workflow.getName(),
            LocalDateTime.now()
        );
    }

//    /**
//     * 记录执行开始
//     */
//    private void recordExecutionStart(Workflow workflow, WorkflowExecutionContext context) {
//        // 更新统计
//        activeExecutions.incrementAndGet();
//        totalExecutions.incrementAndGet();
//
//        // 记录性能日志
//        performanceLogger.logWorkflowStart(workflow.getId(), workflow.getName());
//
//        // 记录审计日志
//        auditLogger.logWorkflowStarted(workflow.getId(), workflow.getName(), "system",
//            Map.of("startTime", context.getStartTime()));
//
//        // 发布事件
//        eventPublisher.publishEvent(new WorkflowEvent.WorkflowStarted(
//            workflow.getWorkflowId(), workflow.getName()));
//    }

    /**
     * 执行工作流核心逻辑
     */
    private WorkflowExecutionResult doExecuteWorkflow(
            Workflow workflow,
            WorkflowExecutionContext context,
            WorkflowOrchestrator orchestrator) {

        try {
            // 使用执行引擎执行工作流
            CompletableFuture<Void> executionFuture = executionEngine.executeWorkflow(workflow);

            // 等待执行完成
            executionFuture.get();

            // 检查执行结果
            if (workflow.getStatus() == WorkflowStatus.COMPLETED) {
                return WorkflowExecutionResult.success(
                    workflow.getWorkflowId(),
                    "工作流执行成功",
                    workflow.getGlobalContext()
                );
            } else if (workflow.getStatus() == WorkflowStatus.FAILED) {
                return WorkflowExecutionResult.failure(
                    "工作流执行失败: " //+ workflow.getErrorMessage()
                );
            } else {
                return WorkflowExecutionResult.failure(
                    "工作流执行异常结束，状态: " + workflow.getStatus()
                );
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("工作流执行被中断", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("工作流执行异常", e.getCause());
        }
    }

//    /**
//     * 记录执行完成
//     */
//    private void recordExecutionComplete(Workflow workflow, WorkflowExecutionContext context, WorkflowExecutionResult result) {
//        // 更新上下文
//        context.setEndTime(LocalDateTime.now());
//        context.setStatus(result.isSuccess() ? "COMPLETED" : "FAILED");
//        if (!result.isSuccess()) {
//            context.setErrorMessage(result.getErrorMessage());
//        }
//
//        // 更新统计
//        activeExecutions.decrementAndGet();
//        if (result.isSuccess()) {
//            successfulExecutions.incrementAndGet();
//        } else {
//            failedExecutions.incrementAndGet();
//        }
//
//        // 记录性能日志
////        performanceLogger.logWorkflowComplete(workflow.getId(), workflow.getName(), result.isSuccess());
////
////        // 记录审计日志
////        auditLogger.logWorkflowCompleted(workflow.getId(), workflow.getName(), "system",
////            result.isSuccess(), result.getErrorMessage());
//
//        // 发布事件
//        if (result.isSuccess()) {
//            eventPublisher.publishEvent(new WorkflowEvent.WorkflowCompleted(
//                workflow.getWorkflowId(), workflow.getName(), LocalDateTime.now()));
//        } else {
//            eventPublisher.publishEvent(new WorkflowEvent.WorkflowFailed(
//                workflow.getId(), result.getErrorMessage(), LocalDateTime.now()));
//        }
//    }
//
    /**
     * 处理执行异常
     */
    private WorkflowExecutionResult handleExecutionError(WorkflowId workflowId, WorkflowExecutionContext context, Exception e) {
        // 更新统计
        activeExecutions.decrementAndGet();
        failedExecutions.incrementAndGet();

        return WorkflowExecutionResult.failure("工作流执行异常: " + e.getMessage());
    }

//    /**
//     * 记录执行错误
//     */
//    private void recordExecutionError(WorkflowId workflowId, WorkflowExecutionContext context, Exception e) {
//        if (context != null) {
//            context.setEndTime(LocalDateTime.now());
//            context.setStatus("ERROR");
//            context.setErrorMessage(e.getMessage());
//        }
//
//        // 记录审计日志
//        auditLogger.logSecurityEvent(workflowId, "EXECUTION_ERROR", "system",
//            "工作流执行异常: " + e.getMessage(), "ERROR");
//
//        // 发布事件
//        eventPublisher.publishEvent(new WorkflowEvent.WorkflowFailed(
//            workflowId, e.getMessage(), LocalDateTime.now()));
//    }
//
    /**
     * 清理执行资源
     */
    private void cleanupExecution(WorkflowId workflowId) {
        executionContexts.remove(workflowId);
        executionFutures.remove(workflowId);
    }

//    /**
//     * 继续执行工作流
//     */
//    private boolean continueExecution(Workflow workflow) {
//        try {
//            CompletableFuture<Void> future = executionEngine.executeWorkflow(workflow);
//            executionFutures.put(workflow.getId(),
//                future.thenApply(v -> WorkflowExecutionResult.success(workflow.getId(), "工作流恢复执行成功")));
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }

    /**
     * 映射工作流状态到执行状态
     */
    private WorkflowExecutionStatus mapWorkflowStatusToExecutionStatus(WorkflowStatus status) {
        return switch (status) {
            case CREATED -> WorkflowExecutionStatus.PENDING;
            case RUNNING -> WorkflowExecutionStatus.RUNNING;
            case PAUSED -> WorkflowExecutionStatus.PAUSED;
            case COMPLETED -> WorkflowExecutionStatus.COMPLETED;
            case FAILED -> WorkflowExecutionStatus.FAILED;
            case CANCELLED -> WorkflowExecutionStatus.CANCELLED;
        };
    }

    /**
     * 计算成功率
     */
    private double calculateSuccessRate() {
        int total = totalExecutions.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) successfulExecutions.get() / total * 100.0;
    }

    // ==================== 内部类 ====================

    /**
     * 工作流执行请求
     */
    public static class WorkflowExecutionRequest {
        private WorkflowId workflowId;
        private Map<String, Object> initialContext;
        private WorkflowOrchestrator orchestrator;

        // 构造函数
        public WorkflowExecutionRequest(WorkflowId workflowId, Map<String, Object> initialContext, WorkflowOrchestrator orchestrator) {
            this.workflowId = workflowId;
            this.initialContext = initialContext != null ? initialContext : new HashMap<>();
            this.orchestrator = orchestrator;
        }

        // Getters
        public WorkflowId getWorkflowId() { return workflowId; }
        public Map<String, Object> getInitialContext() { return initialContext; }
        public WorkflowOrchestrator getOrchestrator() { return orchestrator; }
    }

    /**
     * 工作流执行结果
     */
    public static class WorkflowExecutionResult {
        private final boolean success;
        private final String message;
        private final String errorMessage;
        private final WorkflowId workflowId;
        private final Map<String, Object> resultData;
        private final LocalDateTime completionTime;

        private WorkflowExecutionResult(boolean success, String message, String errorMessage,
                                      WorkflowId workflowId, Map<String, Object> resultData) {
            this.success = success;
            this.message = message;
            this.errorMessage = errorMessage;
            this.workflowId = workflowId;
            this.resultData = resultData != null ? resultData : new HashMap<>();
            this.completionTime = LocalDateTime.now();
        }

        public static WorkflowExecutionResult success(WorkflowId workflowId, String message) {
            return new WorkflowExecutionResult(true, message, null, workflowId, null);
        }

        public static WorkflowExecutionResult success(WorkflowId workflowId, String message, Map<String, Object> resultData) {
            return new WorkflowExecutionResult(true, message, null, workflowId, resultData);
        }

        public static WorkflowExecutionResult failure(String errorMessage) {
            return new WorkflowExecutionResult(false, null, errorMessage, null, null);
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getErrorMessage() { return errorMessage; }
        public WorkflowId getWorkflowId() { return workflowId; }
        public Map<String, Object> getResultData() { return resultData; }
        public LocalDateTime getCompletionTime() { return completionTime; }
    }

    /**
     * 工作流执行状态
     */
    public enum WorkflowExecutionStatus {
        NOT_FOUND,
        PENDING,
        RUNNING,
        PAUSED,
        COMPLETED,
        FAILED,
        CANCELLED
    }

    /**
     * 执行统计信息
     */
    public static class ExecutionStatistics {
        private final int activeExecutions;
        private final int totalExecutions;
        private final int successfulExecutions;
        private final int failedExecutions;
        private final double successRate;

        private ExecutionStatistics(int activeExecutions, int totalExecutions,
                                   int successfulExecutions, int failedExecutions, double successRate) {
            this.activeExecutions = activeExecutions;
            this.totalExecutions = totalExecutions;
            this.successfulExecutions = successfulExecutions;
            this.failedExecutions = failedExecutions;
            this.successRate = successRate;
        }

        public static Builder builder() {
            return new Builder();
        }

        // Getters
        public int getActiveExecutions() { return activeExecutions; }
        public int getTotalExecutions() { return totalExecutions; }
        public int getSuccessfulExecutions() { return successfulExecutions; }
        public int getFailedExecutions() { return failedExecutions; }
        public double getSuccessRate() { return successRate; }

        public static class Builder {
            private int activeExecutions;
            private int totalExecutions;
            private int successfulExecutions;
            private int failedExecutions;
            private double successRate;

            public Builder activeExecutions(int activeExecutions) {
                this.activeExecutions = activeExecutions;
                return this;
            }

            public Builder totalExecutions(int totalExecutions) {
                this.totalExecutions = totalExecutions;
                return this;
            }

            public Builder successfulExecutions(int successfulExecutions) {
                this.successfulExecutions = successfulExecutions;
                return this;
            }

            public Builder failedExecutions(int failedExecutions) {
                this.failedExecutions = failedExecutions;
                return this;
            }

            public Builder successRate(double successRate) {
                this.successRate = successRate;
                return this;
            }

            public ExecutionStatistics build() {
                return new ExecutionStatistics(activeExecutions, totalExecutions,
                    successfulExecutions, failedExecutions, successRate);
            }
        }
    }
}