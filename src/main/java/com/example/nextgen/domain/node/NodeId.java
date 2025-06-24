package com.example.nextgen.domain.node;

import java.util.Objects;
import java.util.UUID;

/**
 * 节点标识符值对象
 */
public class NodeId {
    private final String value;

    private NodeId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("NodeId cannot be null or empty");
        }
        this.value = value;
    }

    public static NodeId generate() {
        return new NodeId(UUID.randomUUID().toString());
    }

    public static NodeId of(String value) {
        return new NodeId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeId nodeId = (NodeId) o;
        return Objects.equals(value, nodeId.value);
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