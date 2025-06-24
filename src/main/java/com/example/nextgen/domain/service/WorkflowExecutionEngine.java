package com.example.nextgen.domain.service;

import com.example.nextgen.domain.event.WorkflowEvent;
import com.example.nextgen.domain.node.NodeStatus;
import com.example.nextgen.domain.node.WorkflowNode;
import com.example.nextgen.domain.workflow.Workflow;
import com.example.nextgen.domain.workflow.WorkflowStatus;
import com.example.nextgen.domain.orchestration.WorkflowOrchestrator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 工作流执行引擎领域服务
 * 负责工作流的执行调度和生命周期管理
 */
@Slf4j
@Service
public class WorkflowExecutionEngine {
    
    private final ApplicationEventPublisher eventPublisher;
//    private final AgentExecutor agentExecutor;
    
    public WorkflowExecutionEngine(ApplicationEventPublisher eventPublisher//,
//                                 AgentExecutor agentExecutor
    ) {
        this.eventPublisher = eventPublisher;
//        this.agentExecutor = agentExecutor;
    }
    
    /**
     * 启动工作流执行
     */
    public CompletableFuture<Void> startExecution(Workflow workflow, WorkflowOrchestrator orchestrator) {
        return executeWorkflow(workflow);
    }
    
    /**
     * 执行工作流
     */
    public CompletableFuture<Void> executeWorkflow(Workflow workflow) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 启动工作流
                workflow.start();
                publishDomainEvents(workflow);
                
                // 执行工作流循环
                executeWorkflowLoop(workflow);
                
            } catch (Exception e) {
                workflow.fail("Workflow execution failed: " + e.getMessage());
                publishDomainEvents(workflow);
                throw new RuntimeException("Workflow execution failed", e);
            }
        });
    }
    
    /**
     * 工作流执行循环
     */
    private void executeWorkflowLoop(Workflow workflow) {
        while (workflow.getStatus() == WorkflowStatus.RUNNING) {
            List<WorkflowNode> executableNodes = workflow.getExecutableNodes();
            
            if (executableNodes.isEmpty()) {
                // 检查是否有正在运行的节点
                List<WorkflowNode> runningNodes = workflow.getRunningNodes();
                if (runningNodes.isEmpty()) {
                    // 没有可执行和正在运行的节点，工作流可能已完成或卡住
                    break;
                }
                // 等待正在运行的节点完成
                waitForRunningNodes(runningNodes);
                continue;
            }
            
            // 执行所有可执行的节点
            for (WorkflowNode node : executableNodes) {
                log.info("Executing node: " + node.getName());
                executeNode(workflow, node);
            }
        }
    }
    
    /**
     * 执行单个节点
     */
    private void executeNode(Workflow workflow, WorkflowNode node) {
        try {
            node.setInputData(workflow.getGlobalContext());
            // 启动节点
            node.start();
            publishNodeEvents(node);
            node.complete(Map.of("result", "success"));
            workflow.onNodeCompleted(node.getNodeId());
        } catch (Exception e) {
            node.fail("Node execution failed: " + e.getMessage());
            workflow.onNodeFailed(node.getNodeId(), e.getMessage());
            publishNodeEvents(node);
            publishDomainEvents(workflow);
        }

        publishNodeEvents(node);
        publishDomainEvents(workflow);
    }
    
//    /**
//     * 执行条件节点
//     */
//    private void executeConditionNode(Workflow workflow, WorkflowNode node) {
//        // 简单实现：根据输入数据中的条件字段判断
//        Map<String, Object> inputData = node.getInputData();
//        Object condition = inputData.get("condition");
//
//        boolean result = evaluateCondition(condition);
//        node.complete(Map.of("conditionResult", result));
//    }
//
//    /**
//     * 执行并行节点
//     */
//    private void executeParallelNode(Workflow workflow, WorkflowNode node) {
//        // 并行节点的实现需要更复杂的逻辑
//        // 这里简单实现为直接完成
//        node.complete(Map.of("parallelResult", "completed"));
//    }
    
//    /**
//     * 执行聚合节点
//     */
//    private void executeAggregatorNode(Workflow workflow, WorkflowNode node) {
//        // 聚合节点收集前置节点的输出
//        Map<String, Object> aggregatedData = aggregateInputData(workflow, node);
//        node.complete(aggregatedData);
//    }
    
    /**
     * 等待正在运行的节点完成
     */
    private void waitForRunningNodes(List<WorkflowNode> runningNodes) {
        try {
            Thread.sleep(1000); // 简单的等待实现
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 聚合输入数据
     */
    private Map<String, Object> aggregateInputData(Workflow workflow, WorkflowNode node) {
        // 简单实现：返回全局上下文
        return workflow.getGlobalContext();
    }
    
    /**
     * 发布领域事件
     */
    private void publishDomainEvents(Workflow workflow) {
        workflow.getDomainEvents().forEach(eventPublisher::publishEvent);
        workflow.clearDomainEvents();
    }
    
    /**
     * 发布节点事件
     */
    private void publishNodeEvents(WorkflowNode node) {
        log.info("Publishing node events: " + node.getName());
        node.getDomainEvents().forEach(eventPublisher::publishEvent);
        node.clearDomainEvents();
    }
}