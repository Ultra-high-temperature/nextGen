package com.example.nextgen.domain.event;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 工作流执行上下文
 * 用于跟踪工作流执行过程中的状态和数据
 */
public class WorkflowExecutionContext {
    private final String workflowId;
    private final String workflowName;
    private final LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String errorMessage;
    private final Map<String, Object> contextData;
    private int totalNodes;
    private int completedNodes;
    private int failedNodes;
    
    public WorkflowExecutionContext(String workflowId, String workflowName, LocalDateTime startTime) {
        this.workflowId = workflowId;
        this.workflowName = workflowName;
        this.startTime = startTime;
        this.status = "RUNNING";
        this.contextData = new HashMap<>();
        this.totalNodes = 0;
        this.completedNodes = 0;
        this.failedNodes = 0;
    }
    
    public void markCompleted(LocalDateTime endTime) {
        this.endTime = endTime;
        this.status = "COMPLETED";
    }
    
    public void markFailed(LocalDateTime endTime, String errorMessage) {
        this.endTime = endTime;
        this.status = "FAILED";
        this.errorMessage = errorMessage;
    }
    
    public void incrementCompletedNodes() {
        this.completedNodes++;
    }
    
    public void incrementFailedNodes() {
        this.failedNodes++;
    }
    
    public void setTotalNodes(int totalNodes) {
        this.totalNodes = totalNodes;
    }
    
    public void addContextData(String key, Object value) {
        this.contextData.put(key, value);
    }
    
    public double getProgress() {
        if (totalNodes == 0) return 0.0;
        return (double) (completedNodes + failedNodes) / totalNodes;
    }
    
    public long getDurationInSeconds() {
        LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
        return java.time.Duration.between(startTime, end).getSeconds();
    }
    
    // Getters
    public String getWorkflowId() { return workflowId; }
    public String getWorkflowName() { return workflowName; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public String getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
    public Map<String, Object> getContextData() { return new HashMap<>(contextData); }
    public int getTotalNodes() { return totalNodes; }
    public int getCompletedNodes() { return completedNodes; }
    public int getFailedNodes() { return failedNodes; }
}