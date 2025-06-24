//package com.example.nextgen.domain.application;
package com.example.nextgen.domain.application;

import com.example.nextgen.domain.workflow.WorkflowId;
import com.example.nextgen.domain.workflow.Workflow;
import com.example.nextgen.domain.workflow.WorkflowStatus;
import com.example.nextgen.domain.node.WorkflowNode;
import com.example.nextgen.domain.node.NodeId;
import com.example.nextgen.domain.node.NodeType;
import com.example.nextgen.domain.node.NodeStatus;
import com.example.nextgen.domain.event.EventLog;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工作流应用服务的结果类集合
 */
public class WorkflowResults {
    /**
     * 工作流创建结果
     */
    @Getter
    public static class WorkflowCreationResult {
        private final boolean success;
        private final String message;
        private final WorkflowId workflowId;
        private final String workflowName;

        private WorkflowCreationResult(boolean success, String message, WorkflowId workflowId, String workflowName) {
            this.success = success;
            this.message = message;
            this.workflowId = workflowId;
            this.workflowName = workflowName;
        }

        public static WorkflowCreationResult success(WorkflowId workflowId, String workflowName) {
            return new WorkflowCreationResult(true, "工作流创建成功", workflowId, workflowName);
        }

        public static WorkflowCreationResult failure(String message) {
            return new WorkflowCreationResult(false, message, null, null);
        }
    }
    /**
     * 工作流执行结果
     */
    @Getter
    public static class WorkflowExecutionResult {
        private final boolean success;
        private final String message;
        private final WorkflowId workflowId;
        private final LocalDateTime startTime;

        private WorkflowExecutionResult(boolean success, String message, WorkflowId workflowId, LocalDateTime startTime) {
            this.success = success;
            this.message = message;
            this.workflowId = workflowId;
            this.startTime = startTime;
        }

        public static WorkflowExecutionResult success(WorkflowId workflowId, String message) {
            return new WorkflowExecutionResult(true, message, workflowId, LocalDateTime.now());
        }

        public static WorkflowExecutionResult failure(String message) {
            return new WorkflowExecutionResult(false, message, null, null);
        }

    }

    /**
     * 工作流操作结果
     */
    @Getter
    public static class WorkflowOperationResult {
        private final boolean success;
        private final String message;
        private final LocalDateTime timestamp;

        private WorkflowOperationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
            this.timestamp = LocalDateTime.now();
        }

        public static WorkflowOperationResult success(String message) {
            return new WorkflowOperationResult(true, message);
        }

        public static WorkflowOperationResult failure(String message) {
            return new WorkflowOperationResult(false, message);
        }

    }
    /**
     * 工作流状态查询结果
     */
    @Getter
    public static class WorkflowStatusResult {
        private final boolean found;
        private final String errorMessage;
        private final WorkflowId workflowId;
        private final String workflowName;
        private final WorkflowStatus status;
        private final List<NodeStatusInfo> nodeStatuses;
        private final Map<String, Object> globalContext;
        private final double progress;

        private WorkflowStatusResult(boolean found, String errorMessage, WorkflowId workflowId,
                                   String workflowName, WorkflowStatus status,
                                   List<NodeStatusInfo> nodeStatuses, Map<String, Object> globalContext,
                                   double progress) {
            this.found = found;
            this.errorMessage = errorMessage;
            this.workflowId = workflowId;
            this.workflowName = workflowName;
            this.status = status;
            this.nodeStatuses = nodeStatuses;
            this.globalContext = globalContext;
            this.progress = progress;
        }

        public static WorkflowStatusResult success(Workflow workflow) {
            List<NodeStatusInfo> nodeStatuses = workflow.getNodes().values().stream()
                    .map(NodeStatusInfo::from)
                    .collect(Collectors.toList());

            double progress = calculateProgress(workflow);

            return new WorkflowStatusResult(true, null, workflow.getWorkflowId(), workflow.getName(),
                                          workflow.getStatus(), nodeStatuses, workflow.getGlobalContext(), progress);
        }

        public static WorkflowStatusResult notFound() {
            return new WorkflowStatusResult(false, "工作流不存在", null, null, null, null, null, 0.0);
        }

        public static WorkflowStatusResult error(String errorMessage) {
            return new WorkflowStatusResult(false, errorMessage, null, null, null, null, null, 0.0);
        }

        private static double calculateProgress(Workflow workflow) {
            int totalNodes = workflow.getNodes().size();
            if (totalNodes == 0) return 0.0;

            long completedNodes = workflow.getNodes().values().stream()
                    .filter(node -> node.getStatus() == NodeStatus.COMPLETED)
                    .count();

            return (double) completedNodes / totalNodes * 100.0;
        }
    }

    /**
     * 节点状态信息
     */
    @Getter
    public static class NodeStatusInfo {
        private final NodeId nodeId;
        private final String nodeName;
        private final NodeType nodeType;
        private final NodeStatus status;
        private final Map<String, Object> inputData;
        private final Map<String, Object> outputData;
        private final Collection<NodeId> dependencies;

        private NodeStatusInfo(NodeId nodeId, String nodeName, NodeType nodeType, NodeStatus status, Map<String, Object> inputData, Map<String, Object> outputData,
                               Collection<NodeId> dependencies) {
            this.nodeId = nodeId;
            this.nodeName = nodeName;
            this.nodeType = nodeType;
            this.status = status;
            this.inputData = inputData;
            this.outputData = outputData;
            this.dependencies = dependencies;
        }

        public static NodeStatusInfo from(WorkflowNode node) {
            return new NodeStatusInfo(
                    node.getNodeId(),
                    node.getName(),
                    node.getType(),
                    node.getStatus(),
                    node.getInputData(),
                    node.getOutputData(),
                    node.getDependencies()
            );
        }

    }
//
    /**
     * 工作流摘要信息
     */
    public static class WorkflowSummary {
        private final WorkflowId workflowId;
        private final String name;
        private final String description;
        private final WorkflowStatus status;
        private final int totalNodes;
        private final int completedNodes;
        private final double progress;
        private final LocalDateTime lastUpdated;

        public WorkflowSummary(WorkflowId workflowId, String name, String description,
                             WorkflowStatus status, int totalNodes, int completedNodes, double progress) {
            this.workflowId = workflowId;
            this.name = name;
            this.description = description;
            this.status = status;
            this.totalNodes = totalNodes;
            this.completedNodes = completedNodes;
            this.progress = progress;
            this.lastUpdated = LocalDateTime.now();
        }

        // Getters
        public WorkflowId getWorkflowId() { return workflowId; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public WorkflowStatus getStatus() { return status; }
        public int getTotalNodes() { return totalNodes; }
        public int getCompletedNodes() { return completedNodes; }
        public double getProgress() { return progress; }
        public LocalDateTime getLastUpdated() { return lastUpdated; }
    }

    /**
     * 工作流执行历史
     */
    public static class WorkflowExecutionHistory {
        private final boolean found;
        private final String errorMessage;
        private final WorkflowId workflowId;
        private final List<EventLog> eventLogs;
        private final Map<String, Object> executionStatistics;

        private WorkflowExecutionHistory(boolean found, String errorMessage, WorkflowId workflowId,
                                       List<EventLog> eventLogs, Map<String, Object> executionStatistics) {
            this.found = found;
            this.errorMessage = errorMessage;
            this.workflowId = workflowId;
            this.eventLogs = eventLogs;
            this.executionStatistics = executionStatistics;
        }

        public static WorkflowExecutionHistory success(WorkflowId workflowId, List<EventLog> eventLogs,
                                                      Map<String, Object> statistics) {
            return new WorkflowExecutionHistory(true, null, workflowId, eventLogs, statistics);
        }

        public static WorkflowExecutionHistory notFound() {
            return new WorkflowExecutionHistory(false, "工作流不存在", null, null, null);
        }

        public static WorkflowExecutionHistory error(String errorMessage) {
            return new WorkflowExecutionHistory(false, errorMessage, null, null, null);
        }

        // Getters
        public boolean isFound() { return found; }
        public String getErrorMessage() { return errorMessage; }
        public WorkflowId getWorkflowId() { return workflowId; }
        public List<EventLog> getEventLogs() { return eventLogs; }
        public Map<String, Object> getExecutionStatistics() { return executionStatistics; }
    }
    /**
     * 工作流验证结果
     */
    public static class WorkflowValidationResult {
        private final boolean valid;
        private final List<String> errors;
        private final List<String> warnings;

        private WorkflowValidationResult(boolean valid, List<String> errors, List<String> warnings) {
            this.valid = valid;
            this.errors = errors;
            this.warnings = warnings;
        }

        public static WorkflowValidationResult success() {
            return new WorkflowValidationResult(true, List.of(), List.of());
        }

        public static WorkflowValidationResult success(List<String> warnings) {
            return new WorkflowValidationResult(true, List.of(), warnings);
        }

        public static WorkflowValidationResult failure(List<String> errors) {
            return new WorkflowValidationResult(false, errors, List.of());
        }

        public static WorkflowValidationResult failure(List<String> errors, List<String> warnings) {
            return new WorkflowValidationResult(false, errors, warnings);
        }

        // Getters
        public boolean isValid() { return valid; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }

        public boolean hasWarnings() {
            return warnings != null && !warnings.isEmpty();
        }

        public boolean hasErrors() {
            return errors != null && !errors.isEmpty();
        }
    }
    /**
     * 工作流详情结果
     */
    public static class WorkflowDetailResult {
        private final WorkflowId workflowId;
        private final String name;
        private final String description;
        private final WorkflowStatus status;
        private final List<NodeInfo> nodes;
        private final List<EdgeInfo> edges;
        private final Map<String, Object> globalContext;
        private final LocalDateTime createdAt;
        private final LocalDateTime updatedAt;
        private final LocalDateTime startedAt;
        private final LocalDateTime completedAt;

        public WorkflowDetailResult(WorkflowId workflowId, String name, String description,
                                  WorkflowStatus status, List<NodeInfo> nodes, List<EdgeInfo> edges,
                                  Map<String, Object> globalContext,
                                  LocalDateTime createdAt, LocalDateTime updatedAt,
                                  LocalDateTime startedAt, LocalDateTime completedAt) {
            this.workflowId = workflowId;
            this.name = name;
            this.description = description;
            this.status = status;
            this.nodes = nodes;
            this.edges = edges;
            this.globalContext = globalContext;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
            this.startedAt = startedAt;
            this.completedAt = completedAt;
        }

        // Getters
        public WorkflowId getWorkflowId() { return workflowId; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public WorkflowStatus getStatus() { return status; }
        public List<NodeInfo> getNodes() { return nodes; }
        public List<EdgeInfo> getEdges() { return edges; }
        public Map<String, Object> getGlobalContext() { return globalContext; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public LocalDateTime getStartedAt() { return startedAt; }
        public LocalDateTime getCompletedAt() { return completedAt; }
    }

    /**
     * 节点信息
     */
    public static class NodeInfo {
        private final String nodeId;
        private final String type;
        private final String name;
        private final String status;
        private final Map<String, Object> properties;
        private final LocalDateTime createdAt;
        private final LocalDateTime updatedAt;

        public NodeInfo(String nodeId, String type, String name, String status,
                       Map<String, Object> properties, LocalDateTime createdAt, LocalDateTime updatedAt) {
            this.nodeId = nodeId;
            this.type = type;
            this.name = name;
            this.status = status;
            this.properties = properties;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        // Getters
        public String getNodeId() { return nodeId; }
        public String getType() { return type; }
        public String getName() { return name; }
        public String getStatus() { return status; }
        public Map<String, Object> getProperties() { return properties; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
    }

    /**
     * 边信息
     */
    @Getter
    public static class EdgeInfo {
        private final String edgeId;
        private final String sourceNodeId;
        private final String targetNodeId;
        private final String name;
        private final String type;
        private final String condition;
        private final Map<String, Object> properties;
        private final Integer priority;
        private final boolean enabled;
        private final String description;

        public EdgeInfo(String edgeId, String sourceNodeId, String targetNodeId, String name,
                       String type, String condition, Map<String, Object> properties,
                       Integer priority, boolean enabled, String description) {
            this.edgeId = edgeId;
            this.sourceNodeId = sourceNodeId;
            this.targetNodeId = targetNodeId;
            this.name = name;
            this.type = type;
            this.condition = condition;
            this.properties = properties;
            this.priority = priority;
            this.enabled = enabled;
            this.description = description;
        }
    }

    /**
     * 工作流性能统计
     */
    @Getter
    public static class WorkflowPerformanceStats {
        private final WorkflowId workflowId;
        private final long totalExecutionTime;
        private final long averageNodeExecutionTime;
        private final int totalNodes;
        private final int successfulNodes;
        private final int failedNodes;
        private final double successRate;
        private final Map<String, Long> agentExecutionTimes;

        public WorkflowPerformanceStats(WorkflowId workflowId, long totalExecutionTime,
                                      long averageNodeExecutionTime, int totalNodes,
                                      int successfulNodes, int failedNodes, double successRate,
                                      Map<String, Long> agentExecutionTimes) {
            this.workflowId = workflowId;
            this.totalExecutionTime = totalExecutionTime;
            this.averageNodeExecutionTime = averageNodeExecutionTime;
            this.totalNodes = totalNodes;
            this.successfulNodes = successfulNodes;
            this.failedNodes = failedNodes;
            this.successRate = successRate;
            this.agentExecutionTimes = agentExecutionTimes;
        }
    }
}