package com.example.nextgen.domain.orchestration;

import com.example.nextgen.domain.node.NodeId;
import com.example.nextgen.domain.node.NodeStatus;
import com.example.nextgen.domain.node.NodeType;
import com.example.nextgen.domain.node.WorkflowNode;
import com.example.nextgen.domain.workflow.Workflow;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 静态编排器实现
 * 基于预定义的依赖关系和节点配置进行编排
 */
@Component
public class StaticOrchestrator implements WorkflowOrchestrator {
    
    @Override
    public List<WorkflowNode> getNextExecutableNodes(Workflow workflow) {
        return workflow.getNodes().values().stream()
                .filter(node -> canExecuteNode(node, workflow))
                .sorted(Comparator.comparingInt(node -> getNodePriority(node, workflow)))
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean canExecuteNode(WorkflowNode node, Workflow workflow) {
        // 节点必须处于空闲或等待状态
        if (node.getStatus() != NodeStatus.IDLE && node.getStatus() != NodeStatus.WAITING) {
            return false;
        }

        // 检查所有依赖节点是否已完成
        for (NodeId dependencyId : (Set<NodeId>)node.getDependencies()) {
            WorkflowNode dependency = findNodeById(workflow, dependencyId);
            if (dependency == null || dependency.getStatus() != NodeStatus.COMPLETED) {
                return false;
            }
        }
        
        // 特殊节点类型的检查
        return switch (node.getType()) {
            case START ->
                // 开始节点可以直接执行
                    true;
            case END ->
                // 结束节点需要所有非结束节点都完成
                    areAllNonEndNodesCompleted(workflow);
            case CONDITION ->
                // 条件节点需要有输入数据
                    node.getInputData() != null && !node.getInputData().isEmpty();
            case PARALLEL ->
                // 并行节点可以执行
                    true;
            case AGGREGATOR ->
                // 聚合节点需要等待所有并行分支完成
                    areParallelBranchesCompleted(workflow, node);
            default -> true;
        };
    }
    
    @Override
    public int getNodePriority(WorkflowNode node, Workflow workflow) {
        // 基于节点类型和依赖深度计算优先级
        int typePriority = getTypePriority(node.getType());
        int dependencyDepth = calculateDependencyDepth(workflow, node);
        
        // 优先级 = 类型优先级 * 100 + 依赖深度
        return typePriority * 100 + dependencyDepth;
    }
    
    @Override
    public void adjustExecutionOrder(Workflow workflow, WorkflowNode completedNode, Map<String, Object> executionResult) {
        // 静态编排器不进行动态调整
        // 但可以根据执行结果更新后续节点的输入数据
        updateDependentNodesInput(workflow, completedNode, executionResult);
    }
    
    @Override
    public boolean isWorkflowCompleted(Workflow workflow) {
        // 检查是否有END节点完成，或者所有节点都已完成
        boolean hasCompletedEndNode = workflow.getNodes().values().stream()
                .anyMatch(node -> node.getType() == NodeType.END && node.getStatus() == NodeStatus.COMPLETED);
        
        if (hasCompletedEndNode) {
            return true;
        }
        
        // 如果没有END节点，检查所有节点是否都已完成
        boolean hasEndNode = workflow.getNodes().values().stream()
                .anyMatch(node -> node.getType() == NodeType.END);
        
        if (!hasEndNode) {
            return workflow.getNodes().values().stream()
                    .allMatch(node -> node.getStatus() == NodeStatus.COMPLETED || 
                             node.getStatus() == NodeStatus.SKIPPED);
        }
        
        return false;
    }
    
    @Override
    public OrchestrationType getOrchestrationType() {
        return OrchestrationType.STATIC;
    }
    
    @Override
    public ValidationResult validateWorkflowStructure(Workflow workflow) {
        // 检查循环依赖
        if (hasCyclicDependencies(workflow)) {
            return ValidationResult.failure("工作流存在循环依赖");
        }
        
        // 检查是否有孤立节点
        if (hasOrphanedNodes(workflow)) {
            return ValidationResult.failure("工作流存在孤立节点");
        }
        
        // 检查START和END节点
        long startNodeCount = workflow.getNodes().values().stream()
                .filter(node -> node.getType() == NodeType.START)
                .count();
        
        if (startNodeCount == 0) {
            return ValidationResult.failure("工作流必须包含至少一个START节点");
        }
        
        if (startNodeCount > 1) {
            return ValidationResult.failure("工作流只能包含一个START节点");
        }
        
        return ValidationResult.success();
    }
    
    // 私有辅助方法
    
    private WorkflowNode findNodeById(Workflow workflow, NodeId nodeId) {
        return workflow.getNodes().values().stream()
                .filter(node -> node.getNodeId().equals(nodeId))
                .findFirst()
                .orElse(null);
    }
    
    private boolean areAllNonEndNodesCompleted(Workflow workflow) {
        return workflow.getNodes().values().stream()
                .filter(node -> node.getType() != NodeType.END)
                .allMatch(node -> node.getStatus() == NodeStatus.COMPLETED || 
                         node.getStatus() == NodeStatus.SKIPPED);
    }
    
    private boolean areParallelBranchesCompleted(Workflow workflow, WorkflowNode aggregatorNode) {
        // 查找所有指向此聚合节点的并行节点
        List<WorkflowNode> parallelNodes = workflow.getNodes().values().stream()
                .filter(node -> node.getType() == NodeType.PARALLEL)
                .filter(node -> node.getDependencies().contains(aggregatorNode.getNodeId()))
                .toList();
        
        return parallelNodes.stream()
                .allMatch(node -> node.getStatus() == NodeStatus.COMPLETED);
    }
    
    private int getTypePriority(NodeType type) {
        return switch (type) {
            case START -> 1;
            case CONDITION -> 3;
            case PARALLEL -> 4;
            case AGGREGATOR -> 5;
            case SUB_WORKFLOW -> 6;
            case HUMAN_INTERVENTION -> 7;
            case END -> 8;
            default -> 9;
        };
    }
    
    private int calculateDependencyDepth(Workflow workflow, WorkflowNode node) {
        Set<NodeId> visited = new HashSet<>();
        return calculateDepthRecursive(workflow, node, visited);
    }
    
    private int calculateDepthRecursive(Workflow workflow, WorkflowNode node, Set<NodeId> visited) {
        if (visited.contains(node.getNodeId())) {
            return 0; // 避免循环
        }
        
        visited.add(node.getNodeId());
        
        if (node.getDependencies().isEmpty()) {
            return 0;
        }
        
        int maxDepth = 0;
        for (NodeId depId : (Set<NodeId>)node.getDependencies()) {
            WorkflowNode depNode = findNodeById(workflow, depId);
            if (depNode != null) {
                maxDepth = Math.max(maxDepth, calculateDepthRecursive(workflow, depNode, visited));
            }
        }
        
        visited.remove(node.getNodeId());
        return maxDepth + 1;
    }
    
    private void updateDependentNodesInput(Workflow workflow, WorkflowNode completedNode, Map<String, Object> executionResult) {
        // 将完成节点的输出数据传递给依赖它的节点
        workflow.getNodes().values().stream()
                .filter(node -> node.getDependencies().contains(completedNode.getNodeId()))
                .forEach(node -> {
                    Map<String, Object> currentInput = node.getInputData();
                    if (currentInput == null) {
                        currentInput = new HashMap<>();
                    }
                    
                    // 合并执行结果到输入数据
                    currentInput.putAll(executionResult);
                    node.setInputData(currentInput);
                });
    }
    
    private boolean hasCyclicDependencies(Workflow workflow) {
        Set<NodeId> visited = new HashSet<>();
        Set<NodeId> recursionStack = new HashSet<>();
        
        for (WorkflowNode node : workflow.getNodes().values()) {
            if (hasCyclicDependenciesUtil(workflow, node, visited, recursionStack)) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean hasCyclicDependenciesUtil(Workflow workflow, WorkflowNode node, 
                                            Set<NodeId> visited, Set<NodeId> recursionStack) {
        if (recursionStack.contains(node.getNodeId())) {
            return true;
        }
        
        if (visited.contains(node.getNodeId())) {
            return false;
        }
        
        visited.add(node.getNodeId());
        recursionStack.add(node.getNodeId());
        
        for (NodeId depId : (Set<NodeId>)node.getDependencies()) {
            WorkflowNode depNode = findNodeById(workflow, depId);
            if (depNode != null && hasCyclicDependenciesUtil(workflow, depNode, visited, recursionStack)) {
                return true;
            }
        }
        
        recursionStack.remove(node.getNodeId());
        return false;
    }
    
    private boolean hasOrphanedNodes(Workflow workflow) {
        Set<NodeId> reachableNodes = new HashSet<>();
        
        // 从START节点开始遍历
        workflow.getNodes().values().stream()
                .filter(node -> node.getType() == NodeType.START)
                .forEach(startNode -> collectReachableNodes(workflow, startNode, reachableNodes));
        
        // 检查是否所有节点都可达
        return workflow.getNodes().values().stream()
                .anyMatch(node -> !reachableNodes.contains(node.getNodeId()));
    }
    
    private void collectReachableNodes(Workflow workflow, WorkflowNode node, Set<NodeId> reachableNodes) {
        if (reachableNodes.contains(node.getNodeId())) {
            return;
        }
        
        reachableNodes.add(node.getNodeId());
        
        // 查找依赖此节点的其他节点
        workflow.getNodes().values().stream()
                .filter(n -> n.getDependencies().contains(node.getNodeId()))
                .forEach(n -> collectReachableNodes(workflow, n, reachableNodes));
    }
}