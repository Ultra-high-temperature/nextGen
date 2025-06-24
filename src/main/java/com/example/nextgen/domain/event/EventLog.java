package com.example.nextgen.domain.event;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 事件日志实体
 * 用于记录工作流执行过程中的所有事件
 */
@Getter
public class EventLog {
    private String eventId;
    private String eventType;
    private LogLevel level;
    private String workflowId;
    private String nodeId;
    private String agentName;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, Object> metadata;
    
    private EventLog() {
        this.metadata = new HashMap<>();
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private EventLog eventLog;
        
        public Builder() {
            this.eventLog = new EventLog();
        }
        
        public Builder eventId(String eventId) {
            this.eventLog.eventId = eventId;
            return this;
        }
        
        public Builder eventType(String eventType) {
            this.eventLog.eventType = eventType;
            return this;
        }
        
        public Builder level(LogLevel level) {
            this.eventLog.level = level;
            return this;
        }
        
        public Builder workflowId(String workflowId) {
            this.eventLog.workflowId = workflowId;
            return this;
        }
        
        public Builder nodeId(String nodeId) {
            this.eventLog.nodeId = nodeId;
            return this;
        }
        
        public Builder agentName(String agentName) {
            this.eventLog.agentName = agentName;
            return this;
        }
        
        public Builder message(String message) {
            this.eventLog.message = message;
            return this;
        }
        
        public Builder timestamp(LocalDateTime timestamp) {
            this.eventLog.timestamp = timestamp;
            return this;
        }
        
        public Builder metadata(Map<String, Object> metadata) {
            this.eventLog.metadata = new HashMap<>(metadata);
            return this;
        }
        
        public Builder addMetadata(String key, Object value) {
            this.eventLog.metadata.put(key, value);
            return this;
        }
        
        public EventLog build() {
            if (eventLog.timestamp == null) {
                eventLog.timestamp = LocalDateTime.now();
            }
            return eventLog;
        }
    }

    
    @Override
    public String toString() {
        return String.format("[%s] %s - %s [%s] %s", 
                           timestamp, level, eventType, workflowId, message);
    }
}