package com.example.nextgen.domain.edge;

import com.example.nextgen.domain.node.NodeId;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 工作流边（连接）
 * 用于连接工作流中的节点，定义节点间的关系和数据流
 */
public class WorkflowEdge {
    
    private EdgeId id;
    private NodeId sourceNodeId;  // 源节点ID
    private NodeId targetNodeId;  // 目标节点ID
    private String name;          // 边的名称
    private EdgeType type;        // 边的类型
    private String condition;     // 条件表达式（用于条件边）
    private Map<String, Object> properties; // 边的属性
    private Integer priority;     // 优先级（用于排序）
    private boolean enabled;      // 是否启用
    private String description;   // 描述
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 私有构造函数，使用Builder模式
    private WorkflowEdge() {
        this.properties = new HashMap<>();
        this.enabled = true;
        this.priority = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 创建Builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * 检查边是否满足执行条件
     */
    public boolean isConditionMet(Map<String, Object> context) {
        if (condition == null || condition.trim().isEmpty()) {
            return true;
        }
        
        // 简单的条件评估（实际项目中可以使用更复杂的表达式引擎）
        try {
            return evaluateCondition(condition, context);
        } catch (Exception e) {
            // 条件评估失败时默认返回false
            return false;
        }
    }
    
    /**
     * 简单的条件评估
     */
    private boolean evaluateCondition(String condition, Map<String, Object> context) {
        // 支持简单的条件表达式
        // 例如: "status == 'success'", "count > 10", "data != null"
        
        if (condition.contains("==")) {
            String[] parts = condition.split("==");
            if (parts.length == 2) {
                String key = parts[0].trim();
                String expectedValue = parts[1].trim().replace("'", "").replace("\"", "");
                Object actualValue = context.get(key);
                return Objects.equals(String.valueOf(actualValue), expectedValue);
            }
        } else if (condition.contains("!=")) {
            String[] parts = condition.split("!=");
            if (parts.length == 2) {
                String key = parts[0].trim();
                String expectedValue = parts[1].trim().replace("'", "").replace("\"", "");
                Object actualValue = context.get(key);
                return !Objects.equals(String.valueOf(actualValue), expectedValue);
            }
        } else if (condition.contains(">")) {
            String[] parts = condition.split(">");
            if (parts.length == 2) {
                String key = parts[0].trim();
                String expectedValue = parts[1].trim();
                Object actualValue = context.get(key);
                if (actualValue instanceof Number && isNumeric(expectedValue)) {
                    return ((Number) actualValue).doubleValue() > Double.parseDouble(expectedValue);
                }
            }
        } else if (condition.contains("<")) {
            String[] parts = condition.split("<");
            if (parts.length == 2) {
                String key = parts[0].trim();
                String expectedValue = parts[1].trim();
                Object actualValue = context.get(key);
                if (actualValue instanceof Number && isNumeric(expectedValue)) {
                    return ((Number) actualValue).doubleValue() < Double.parseDouble(expectedValue);
                }
            }
        } else if (condition.contains("null")) {
            String key = condition.replace("!= null", "").replace("== null", "").trim();
            Object actualValue = context.get(key);
            if (condition.contains("!= null")) {
                return actualValue != null;
            } else {
                return actualValue == null;
            }
        }
        
        return false;
    }
    
    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 更新边的属性
     */
    public void updateProperty(String key, Object value) {
        this.properties.put(key, value);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 获取边的属性
     */
    public Object getProperty(String key) {
        return this.properties.get(key);
    }
    
    /**
     * 设置条件
     */
    public void setCondition(String condition) {
        this.condition = condition;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 启用/禁用边
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters
    public EdgeId getId() { return id; }
    public NodeId getSourceNodeId() { return sourceNodeId; }
    public NodeId getTargetNodeId() { return targetNodeId; }
    public String getName() { return name; }
    public EdgeType getType() { return type; }
    public String getCondition() { return condition; }
    public Map<String, Object> getProperties() { return new HashMap<>(properties); }
    public Integer getPriority() { return priority; }
    public boolean isEnabled() { return enabled; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkflowEdge that = (WorkflowEdge) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "WorkflowEdge{" +
                "id=" + id +
                ", sourceNodeId=" + sourceNodeId +
                ", targetNodeId=" + targetNodeId +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", enabled=" + enabled +
                '}';
    }
    
    /**
     * Builder模式
     */
    public static class Builder {
        private final WorkflowEdge edge;
        
        private Builder() {
            this.edge = new WorkflowEdge();
        }
        
        public Builder id(EdgeId id) {
            edge.id = id;
            return this;
        }
        
        public Builder sourceNodeId(NodeId sourceNodeId) {
            edge.sourceNodeId = sourceNodeId;
            return this;
        }
        
        public Builder targetNodeId(NodeId targetNodeId) {
            edge.targetNodeId = targetNodeId;
            return this;
        }
        
        public Builder name(String name) {
            edge.name = name;
            return this;
        }
        
        public Builder type(EdgeType type) {
            edge.type = type;
            return this;
        }
        
        public Builder condition(String condition) {
            edge.condition = condition;
            return this;
        }
        
        public Builder priority(Integer priority) {
            edge.priority = priority;
            return this;
        }
        
        public Builder enabled(boolean enabled) {
            edge.enabled = enabled;
            return this;
        }
        
        public Builder description(String description) {
            edge.description = description;
            return this;
        }
        
        public Builder property(String key, Object value) {
            edge.properties.put(key, value);
            return this;
        }
        
        public Builder properties(Map<String, Object> properties) {
            edge.properties.putAll(properties);
            return this;
        }
        
        public WorkflowEdge build() {
            // 验证必要字段
            if (edge.id == null) {
                edge.id = EdgeId.generate();
            }
            if (edge.sourceNodeId == null || edge.targetNodeId == null) {
                throw new IllegalArgumentException("源节点ID和目标节点ID不能为空");
            }
            if (edge.type == null) {
                edge.type = EdgeType.SEQUENCE;
            }
            if (edge.name == null || edge.name.trim().isEmpty()) {
                edge.name = edge.sourceNodeId.getValue() + " -> " + edge.targetNodeId.getValue();
            }
            
            return edge;
        }
    }
}