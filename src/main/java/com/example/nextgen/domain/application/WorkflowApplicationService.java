package com.example.nextgen.domain.application;

import com.example.nextgen.domain.edge.WorkflowEdge;
import com.example.nextgen.domain.node.NodeId;
import com.example.nextgen.domain.node.WorkflowNode;
import com.example.nextgen.domain.orchestration.WorkflowOrchestrator;
import com.example.nextgen.domain.service.WorkFlowExecutor;
import com.example.nextgen.domain.service.WorkflowExecutionEngine;
import com.example.nextgen.domain.workflow.Workflow;
import com.example.nextgen.domain.workflow.WorkflowId;
import com.example.nextgen.domain.workflow.WorkflowStatus;
// import com.example.nextgen.domain.event.WorkflowEventHandler; // 已删除
import com.example.nextgen.domain.application.WorkflowResults.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

// 导入缺失的类型
import com.example.nextgen.domain.node.NodeType;
import com.example.nextgen.domain.node.NodeStatus;
import com.example.nextgen.domain.edge.EdgeType;

/**
 * 工作流应用服务
 * 提供工作流的创建、执行、管理等高层业务操作
 */
@Service
public class WorkflowApplicationService {
    // TODO: 需要实现WorkflowRepository接口
    // @Autowired
    // private WorkflowRepository workflowRepository;
    // @Autowired
    // private WorkflowEventHandler eventHandler;

    private final Map<WorkflowId, Workflow> workflowStorage = new HashMap<>();
    private final WorkflowExecutionEngine executionEngine;
    private WorkflowOrchestrator staticOrchestrator;

    public WorkflowApplicationService(WorkflowExecutionEngine executionEngine,WorkflowOrchestrator staticOrchestrator) {
        this.executionEngine = executionEngine;
        this.staticOrchestrator = staticOrchestrator;
    }

    /**
     * 创建新的工作流
     */
    public WorkflowResults.WorkflowCreationResult createWorkflow(CreateWorkflowCommand command) {
        try {
            // 验证命令
            validateCreateCommand(command);
            
            // 创建工作流
            Workflow workflow = Workflow.builder()
                    .workflowId(WorkflowId.generate())
                    .name(command.getName())
                    .description(command.getDescription())
                    .build();
            
            // 用于存储节点名称到NodeId的映射
            Map<String, NodeId> nodeNameToIdMap = new HashMap<>();
            HashMap<WorkflowNode, List<String>> nodeAndDependenceNames = new HashMap<>();
            // 添加节点
            for (CreateNodeCommand nodeCommand : command.getNodes()) {
                WorkflowNode node = nodeCommand.getNodeSupplier().get();
                workflow.addNode(node);
                nodeNameToIdMap.put(nodeCommand.getName(), node.getNodeId());
                nodeAndDependenceNames.put(node, nodeCommand.getDependencies());
            }

            nodeAndDependenceNames.forEach((node, dependencies) -> {
                for (String dependencyName : dependencies) {
                    NodeId dependencyId = nodeNameToIdMap.get(dependencyName);
                    if (dependencyId != null) {
                        node.addDependency(dependencyId);
                    }
                }
            });
            
            // 添加边
            if (command.getEdges() != null) {
                for (CreateEdgeCommand edgeCommand : command.getEdges()) {
                    NodeId sourceNodeId = nodeNameToIdMap.get(edgeCommand.getSourceNodeName());
                    NodeId targetNodeId = nodeNameToIdMap.get(edgeCommand.getTargetNodeName());
                    
                    if (sourceNodeId == null) {
                        throw new IllegalArgumentException("Source node not found: " + edgeCommand.getSourceNodeName());
                    }
                    if (targetNodeId == null) {
                        throw new IllegalArgumentException("Target node not found: " + edgeCommand.getTargetNodeName());
                    }
                    
                    WorkflowEdge edge = createEdge(edgeCommand, sourceNodeId, targetNodeId);
                    workflow.addEdge(edge);
                }
            }
            
            // 验证工作流结构
            WorkflowOrchestrator orchestrator = getOrchestrator(command.getOrchestrationType());
            WorkflowOrchestrator.ValidationResult validation = orchestrator.validateWorkflowStructure(workflow);
            
            if (!validation.isValid()) {
                return WorkflowResults.WorkflowCreationResult.failure(validation.getErrorMessage());
            }
            
            // 保存工作流
            workflowStorage.put(workflow.getWorkflowId(), workflow);
            return WorkflowResults.WorkflowCreationResult.success(workflow.getWorkflowId(), workflow.getName());
            
        } catch (Exception e) {
            return WorkflowResults.WorkflowCreationResult.failure("创建工作流失败: " + e.getMessage());
        }
    }
    
    /**
     * 启动工作流执行
     */
    public CompletableFuture<WorkFlowExecutor.WorkflowExecutionResult> executeWorkflow(ExecuteWorkflowCommand command) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 查找工作流
                Optional<Workflow> workflowOpt = Optional.ofNullable(workflowStorage.get(command.getWorkflowId()));
                if (workflowOpt.isEmpty()) {
                    return WorkFlowExecutor.WorkflowExecutionResult.failure("工作流不存在: " + command.getWorkflowId().getValue());
                }
                
                Workflow workflow = workflowOpt.get();
                
                // 检查工作流状态
                if (workflow.getStatus() == WorkflowStatus.RUNNING) {
                    return WorkFlowExecutor.WorkflowExecutionResult.failure("工作流已在运行中");
                }
                
                // 设置初始上下文
                if (command.getInitialContext() != null) {
                    workflow.getGlobalContext().putAll(command.getInitialContext());
                }
                
                // 选择编排器
                WorkflowOrchestrator orchestrator = getOrchestrator(command.getOrchestrationType());
                
                // 启动执行
                executionEngine.startExecution(workflow, orchestrator);

                // 保存更新后的工作流
                workflowStorage.put(workflow.getWorkflowId(), workflow);


                return WorkFlowExecutor.WorkflowExecutionResult.success(workflow.getWorkflowId(), "工作流启动成功");
                
            } catch (Exception e) {
                return WorkFlowExecutor.WorkflowExecutionResult.failure("启动工作流失败: " + e.getMessage());
            }
        });
    }
    
    /**
     * 暂停工作流执行
     */
    public WorkflowOperationResult pauseWorkflow(WorkflowId workflowId) {
        try {
            Optional<Workflow> workflowOpt = Optional.ofNullable(workflowStorage.get(workflowId));
            if (workflowOpt.isEmpty()) {
                return WorkflowOperationResult.failure("工作流不存在");
            }
            
            Workflow workflow = workflowOpt.get();
            
            if (workflow.getStatus() != WorkflowStatus.RUNNING) {
                return WorkflowOperationResult.failure("只能暂停运行中的工作流");
            }
            
            workflow.pause();
            workflowStorage.put(workflow.getWorkflowId(), workflow);
            
            return WorkflowOperationResult.success("工作流已暂停");
            
        } catch (Exception e) {
            return WorkflowOperationResult.failure("暂停工作流失败: " + e.getMessage());
        }
    }
    
//    /**
//     * 恢复工作流执行
//     */
//    public CompletableFuture<WorkflowOperationResult> resumeWorkflow(ResumeWorkflowCommand command) {
//        return CompletableFuture.supplyAsync(() -> {
//            try {
//                Optional<Workflow> workflowOpt = Optional.ofNullable(workflowStorage.get(command.getWorkflowId()));
//                if (workflowOpt.isEmpty()) {
//                    return WorkflowOperationResult.failure("工作流不存在");
//                }
//
//                Workflow workflow = workflowOpt.get();
//
//                if (workflow.getStatus() != WorkflowStatus.PAUSED) {
//                    return WorkflowOperationResult.failure("只能恢复已暂停的工作流");
//                }
//
//                workflow.resume();
//
//                // 选择编排器并继续执行
//                WorkflowOrchestrator orchestrator = getOrchestrator(command.getOrchestrationType());
//                executionEngine.resumeExecution(workflow, orchestrator);
//
//                workflowStorage.put(workflow.getWorkflowId(), workflow);
//
//                return WorkflowOperationResult.success("工作流已恢复执行");
//
//            } catch (Exception e) {
//                return WorkflowOperationResult.failure("恢复工作流失败: " + e.getMessage());
//            }
//        });
//    }
//
    /**
     * 取消工作流执行
     */
    public WorkflowOperationResult cancelWorkflow(WorkflowId workflowId) {
        try {
            Optional<Workflow> workflowOpt = Optional.ofNullable(workflowStorage.get(workflowId));
            if (workflowOpt.isEmpty()) {
                return WorkflowOperationResult.failure("工作流不存在");
            }
            
            Workflow workflow = workflowOpt.get();
            
            if (workflow.getStatus() == WorkflowStatus.COMPLETED || 
                workflow.getStatus() == WorkflowStatus.CANCELLED) {
                return WorkflowOperationResult.failure("工作流已完成或已取消");
            }
            
            workflow.fail("工作流被取消");
            workflowStorage.put(workflow.getWorkflowId(), workflow);
            
            return WorkflowOperationResult.success("工作流已取消");
            
        } catch (Exception e) {
            return WorkflowOperationResult.failure("取消工作流失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询工作流状态
     */
    public WorkflowStatusResult getWorkflowStatus(WorkflowId workflowId) {
        try {
            Optional<Workflow> workflowOpt = Optional.ofNullable(workflowStorage.get(workflowId));
            if (workflowOpt.isEmpty()) {
                return WorkflowStatusResult.notFound();
            }
            
            Workflow workflow = workflowOpt.get();
            return WorkflowStatusResult.success(workflow);
            
        } catch (Exception e) {
            return WorkflowStatusResult.error("查询工作流状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取工作流列表
     */
    public List<WorkflowSummary> getWorkflowList(WorkflowListQuery query) {
        try {
            List<Workflow> workflows;
            
            if (query.getStatus() != null) {
                workflows = workflowStorage.values().stream()
                    .filter(w -> w.getStatus() == query.getStatus())
                    .collect(Collectors.toList());
            } else if (query.getNamePattern() != null) {
                // 模糊查询实现
                workflows = workflowStorage.values().stream()
                    .filter(w -> w.getName().contains(query.getNamePattern()))
                    .collect(Collectors.toList());
            } else {
                workflows = new ArrayList<>(workflowStorage.values());
            }
            
            return workflows.stream()
                    .map(this::createWorkflowSummary)
                    .sorted(Comparator.comparing(WorkflowSummary::getName))
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
    
    /**
//     * 获取工作流执行历史
//     */
//    public WorkflowExecutionHistory getExecutionHistory(WorkflowId workflowId) {
//        try {
//            Optional<Workflow> workflowOpt = Optional.ofNullable(workflowStorage.get(workflowId));
//            if (workflowOpt.isEmpty()) {
//                return WorkflowExecutionHistory.notFound();
//            }
//
//            Workflow workflow = workflowOpt.get();
//
//            // 从事件处理器获取执行历史
//            return eventHandler.getExecutionHistory(workflowId);
//
//        } catch (Exception e) {
//            return WorkflowExecutionHistory.error("获取执行历史失败: " + e.getMessage());
//        }
//    }
    
    /**
     * 删除工作流
     */
    public WorkflowOperationResult deleteWorkflow(WorkflowId workflowId) {
        try {
            Optional<Workflow> workflowOpt = Optional.ofNullable(workflowStorage.get(workflowId));
            if (workflowOpt.isEmpty()) {
                return WorkflowOperationResult.failure("工作流不存在");
            }
            
            Workflow workflow = workflowOpt.get();
            
            if (workflow.getStatus() == WorkflowStatus.RUNNING) {
                return WorkflowOperationResult.failure("不能删除运行中的工作流");
            }
            
            workflowStorage.remove(workflowId);
            
            return WorkflowOperationResult.success("工作流已删除");
            
        } catch (Exception e) {
            return WorkflowOperationResult.failure("删除工作流失败: " + e.getMessage());
        }
    }
    
    // 私有辅助方法
    
    private void validateCreateCommand(CreateWorkflowCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("创建命令不能为空");
        }
        
        if (command.getName() == null || command.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("工作流名称不能为空");
        }
        
        if (command.getNodes() == null || command.getNodes().isEmpty()) {
            throw new IllegalArgumentException("工作流必须包含至少一个节点");
        }
    }
    
    private WorkflowEdge createEdge(CreateEdgeCommand edgeCommand, NodeId sourceNodeId, NodeId targetNodeId) {
        EdgeType edgeType = EdgeType.valueOf(edgeCommand.getType().toUpperCase());
        
        WorkflowEdge.Builder edgeBuilder = WorkflowEdge.builder()
            .sourceNodeId(sourceNodeId)
            .targetNodeId(targetNodeId)
            .name(edgeCommand.getName())
            .type(edgeType)
            .priority(edgeCommand.getPriority())
            .enabled(edgeCommand.getEnabled())
            .description(edgeCommand.getDescription());
        
        if (edgeCommand.getCondition() != null) {
            edgeBuilder.condition(edgeCommand.getCondition());
        }
        
        if (edgeCommand.getProperties() != null && !edgeCommand.getProperties().isEmpty()) {
            edgeBuilder.properties(edgeCommand.getProperties());
        }
        
        return edgeBuilder.build();
    }
    
    private WorkflowOrchestrator getOrchestrator(WorkflowOrchestrator.OrchestrationType type) {
        return staticOrchestrator; // 默认使用静态编排
    }
    
    private WorkflowSummary createWorkflowSummary(Workflow workflow) {
        return new WorkflowSummary(
                workflow.getWorkflowId(),
                workflow.getName(),
                workflow.getDescription(),
                workflow.getStatus(),
                workflow.getNodes().size(),
                calculateCompletedNodeCount(workflow),
                calculateProgress(workflow)
        );
    }
    
    private int calculateCompletedNodeCount(Workflow workflow) {
        return (int) workflow.getNodes().values().stream()
                .filter(node -> node.getStatus() == NodeStatus.COMPLETED)
                .count();
    }
    
    private double calculateProgress(Workflow workflow) {
        int totalNodes = workflow.getNodes().size();
        if (totalNodes == 0) {
            return 0.0;
        }
        
        int completedNodes = calculateCompletedNodeCount(workflow);
        return (double) completedNodes / totalNodes * 100.0;
    }
    
    // 内部类和结果类
    
    @Setter
    @Getter
    public static class CreateWorkflowCommand {
        // Getters and Setters
        private String name;
        private String description;
        private List<CreateNodeCommand> nodes;
        private List<CreateEdgeCommand> edges = new ArrayList<>();
        private WorkflowOrchestrator.OrchestrationType orchestrationType;

    }
    

    @Data
    public static class CreateNodeCommand {
        private String name;
        private NodeType type;
        private Supplier<WorkflowNode> nodeSupplier;
        private List<String> dependencies;
    }
    
    /**
     * 创建边命令
     */
    @Setter
    @Getter
    public static class CreateEdgeCommand {
        // Getters and Setters
        private String sourceNodeName;
        private String targetNodeName;
        private String name;
        private String type;
        private String condition;
        private Map<String, Object> properties = new HashMap<>();
        private Integer priority = 1;
        private Boolean enabled = true;
        private String description;

    }
    
    @Setter
    @Getter
    public static class ExecuteWorkflowCommand {
        // Getters and Setters
        private WorkflowId workflowId;
        private Map<String, Object> initialContext;
        private WorkflowOrchestrator.OrchestrationType orchestrationType;

    }
    
    public static class ResumeWorkflowCommand {
        private WorkflowId workflowId;
        private WorkflowOrchestrator.OrchestrationType orchestrationType;
        
        // Getters and Setters
        public WorkflowId getWorkflowId() { return workflowId; }
        public void setWorkflowId(WorkflowId workflowId) { this.workflowId = workflowId; }
        
        public WorkflowOrchestrator.OrchestrationType getOrchestrationType() { return orchestrationType; }
        public void setOrchestrationType(WorkflowOrchestrator.OrchestrationType orchestrationType) { this.orchestrationType = orchestrationType; }
    }
    
    public static class WorkflowListQuery {
        private WorkflowStatus status;
        private String namePattern;
        
        // Getters and Setters
        public WorkflowStatus getStatus() { return status; }
        public void setStatus(WorkflowStatus status) { this.status = status; }
        
        public String getNamePattern() { return namePattern; }
        public void setNamePattern(String namePattern) { this.namePattern = namePattern; }
    }
}