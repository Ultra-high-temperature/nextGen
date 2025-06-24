package com.example.nextgen.domain.event;

import com.example.nextgen.domain.node.NodeId;
import com.example.nextgen.domain.workflow.WorkflowId;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 工作流领域事件基类
 */
public abstract class WorkflowEvent {
    private final String eventId;
    private final LocalDateTime occurredAt;
    private final String eventType;

    protected WorkflowEvent(String eventType) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
        this.eventType = eventType;
    }

    public String getEventId() { return eventId; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public String getEventType() { return eventType; }

    // 工作流级别事件
    public static class WorkflowStarted extends WorkflowEvent {
        private final WorkflowId workflowId;
        private final String workflowName;

        public WorkflowStarted(WorkflowId workflowId, String workflowName) {
            super("WorkflowStarted");
            this.workflowId = workflowId;
            this.workflowName = workflowName;
        }

        public WorkflowId getWorkflowId() { return workflowId; }
        public String getWorkflowName() { return workflowName; }
    }

    public static class WorkflowCompleted extends WorkflowEvent {
        private final WorkflowId workflowId;
        private final String workflowName;
        private final Map<String, Object> finalOutput;

        public WorkflowCompleted(WorkflowId workflowId, String workflowName, Map<String, Object> finalOutput) {
            super("WorkflowCompleted");
            this.workflowId = workflowId;
            this.workflowName = workflowName;
            this.finalOutput = finalOutput;
        }

        public WorkflowId getWorkflowId() { return workflowId; }
        public String getWorkflowName() { return workflowName; }
        public Map<String, Object> getFinalOutput() { return finalOutput; }
    }

    public static class WorkflowFailed extends WorkflowEvent {
        private final WorkflowId workflowId;
        private final String workflowName;
        private final String errorMessage;

        public WorkflowFailed(WorkflowId workflowId, String workflowName, String errorMessage) {
            super("WorkflowFailed");
            this.workflowId = workflowId;
            this.workflowName = workflowName;
            this.errorMessage = errorMessage;
        }

        public WorkflowId getWorkflowId() { return workflowId; }
        public String getWorkflowName() { return workflowName; }
        public String getErrorMessage() { return errorMessage; }
    }

    // 节点级别事件
    public static class NodeStarted extends WorkflowEvent {
        private final NodeId nodeId;
        private final String nodeName;

        public NodeStarted(NodeId nodeId, String nodeName) {
            super("NodeStarted");
            this.nodeId = nodeId;
            this.nodeName = nodeName;
        }

        public NodeId getNodeId() { return nodeId; }
        public String getNodeName() { return nodeName; }
    }

    public static class NodeCompleted extends WorkflowEvent {
        private final NodeId nodeId;
        private final String nodeName;
        private final Map<String, Object> output;

        public NodeCompleted(NodeId nodeId, String nodeName, Map<String, Object> output) {
            super("NodeCompleted");
            this.nodeId = nodeId;
            this.nodeName = nodeName;
            this.output = output;
        }

        public NodeId getNodeId() { return nodeId; }
        public String getNodeName() { return nodeName; }
        public Map<String, Object> getOutput() { return output; }
    }

    public static class NodeFailed extends WorkflowEvent {
        private final NodeId nodeId;
        private final String nodeName;
        private final String errorMessage;

        public NodeFailed(NodeId nodeId, String nodeName, String errorMessage) {
            super("NodeFailed");
            this.nodeId = nodeId;
            this.nodeName = nodeName;
            this.errorMessage = errorMessage;
        }

        public NodeId getNodeId() { return nodeId; }
        public String getNodeName() { return nodeName; }
        public String getErrorMessage() { return errorMessage; }
    }

    public static class NodeReset extends WorkflowEvent {
        private final NodeId nodeId;
        private final String nodeName;

        public NodeReset(NodeId nodeId, String nodeName) {
            super("NodeReset");
            this.nodeId = nodeId;
            this.nodeName = nodeName;
        }

        public NodeId getNodeId() { return nodeId; }
        public String getNodeName() { return nodeName; }
    }

    // Agent执行事件
    public static class AgentExecutionStarted extends WorkflowEvent {
        private final NodeId nodeId;
        private final String agentName;
        private final String taskPrompt;

        public AgentExecutionStarted(NodeId nodeId, String agentName, String taskPrompt) {
            super("AgentExecutionStarted");
            this.nodeId = nodeId;
            this.agentName = agentName;
            this.taskPrompt = taskPrompt;
        }

        public NodeId getNodeId() { return nodeId; }
        public String getAgentName() { return agentName; }
        public String getTaskPrompt() { return taskPrompt; }
    }

    public static class AgentExecutionCompleted extends WorkflowEvent {
        private final NodeId nodeId;
        private final String agentName;
        private final Map<String, Object> result;

        public AgentExecutionCompleted(NodeId nodeId, String agentName, Map<String, Object> result) {
            super("AgentExecutionCompleted");
            this.nodeId = nodeId;
            this.agentName = agentName;
            this.result = result;
        }

        public NodeId getNodeId() { return nodeId; }
        public String getAgentName() { return agentName; }
        public Map<String, Object> getResult() { return result; }
    }
}