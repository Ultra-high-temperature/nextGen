package com.example.nextgen.domain.workflow;

import java.util.Objects;
import java.util.UUID;

/**
 * 工作流标识符值对象
 * 遵循DDD设计原则的值对象
 */
public class WorkflowId {
    private final String value;

    private WorkflowId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("WorkflowId cannot be null or empty");
        }
        this.value = value;
    }

    public static WorkflowId generate() {
        return new WorkflowId(UUID.randomUUID().toString());
    }

    public static WorkflowId of(String value) {
        return new WorkflowId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkflowId that = (WorkflowId) o;
        return Objects.equals(value, that.value);
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