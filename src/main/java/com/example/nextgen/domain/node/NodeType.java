package com.example.nextgen.domain.node;

/**
 * 节点类型枚举
 */
public enum NodeType {
    /**
     * 普通节点
     */
    CUSTOM,
    /**
     * 条件判断节点
     */
    CONDITION,
    
    /**
     * 并行执行节点
     */
    PARALLEL,
    
    /**
     * 聚合节点
     */
    AGGREGATOR,
    
    /**
     * 开始节点
     */
    START,
    
    /**
     * 结束节点
     */
    END,
    
    /**
     * 子工作流节点
     */
    SUB_WORKFLOW,
    
    /**
     * 人工干预节点
     */
    HUMAN_INTERVENTION
}