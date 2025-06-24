package com.example.nextgen.domain.event;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 前端事件模型
 * 用于向前端推送经过转换的事件信息
 */
public class FrontendEvent {
    private String eventId;
    private String eventType;
    private String workflowId;
    private String title;
    private String description;
    private String status;
    private double progress;
    private LocalDateTime timestamp;
    private Map<String, Object> data;
    private String category;
    private String priority;
    
    private FrontendEvent() {
        this.data = new HashMap<>();
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private FrontendEvent event;
        
        public Builder() {
            this.event = new FrontendEvent();
        }
        
        public Builder eventId(String eventId) {
            this.event.eventId = eventId;
            return this;
        }
        
        public Builder eventType(String eventType) {
            this.event.eventType = eventType;
            return this;
        }
        
        public Builder workflowId(String workflowId) {
            this.event.workflowId = workflowId;
            return this;
        }
        
        public Builder title(String title) {
            this.event.title = title;
            return this;
        }
        
        public Builder description(String description) {
            this.event.description = description;
            return this;
        }
        
        public Builder status(String status) {
            this.event.status = status;
            return this;
        }
        
        public Builder progress(double progress) {
            this.event.progress = progress;
            return this;
        }
        
        public Builder timestamp(LocalDateTime timestamp) {
            this.event.timestamp = timestamp;
            return this;
        }
        
        public Builder data(Map<String, Object> data) {
            this.event.data = new HashMap<>(data);
            return this;
        }
        
        public Builder addData(String key, Object value) {
            this.event.data.put(key, value);
            return this;
        }
        
        public Builder category(String category) {
            this.event.category = category;
            return this;
        }
        
        public Builder priority(String priority) {
            this.event.priority = priority;
            return this;
        }
        
        public FrontendEvent build() {
            if (event.timestamp == null) {
                event.timestamp = LocalDateTime.now();
            }
            return event;
        }
    }
    
    // Getters
    public String getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public String getWorkflowId() { return workflowId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public double getProgress() { return progress; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public Map<String, Object> getData() { return new HashMap<>(data); }
    public String getCategory() { return category; }
    public String getPriority() { return priority; }
}