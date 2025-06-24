package com.example.nextgen.domain.edge;

import com.example.nextgen.domain.node.NodeId;

import java.util.Objects;
import java.util.UUID;

/**
 * 边ID值对象
 * 用于唯一标识工作流中的边
 */
public class EdgeId {
    
    private final String value;
    
    private EdgeId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("边ID不能为空");
        }
        this.value = value.trim();
    }
    
    /**
     * 创建EdgeId
     */
    public static EdgeId of(String value) {
        return new EdgeId(value);
    }
    
    /**
     * 生成新的EdgeId
     */
    public static EdgeId generate() {
        return new EdgeId("edge_" + UUID.randomUUID().toString().replace("-", ""));
    }
    
    /**
     * 从节点ID生成边ID
     */
    public static EdgeId fromNodes(NodeId sourceId, NodeId targetId) {
        return new EdgeId("edge_" + sourceId.getValue() + "_to_" + targetId.getValue());
    }
    
    /**
     * 获取值
     */
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EdgeId edgeId = (EdgeId) o;
        return Objects.equals(value, edgeId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}