package com.example.nextgen.domain.node;

/**
 * 节点状态枚举
 */
public enum NodeStatus {
    /**
     * 空闲状态 - 节点已创建但未开始执行
     */
    IDLE,
    
    /**
     * 等待状态 - 节点等待依赖完成
     */
    WAITING,
    
    /**
     * 运行中状态 - 节点正在执行
     */
    RUNNING,
    
    /**
     * 已完成状态 - 节点成功完成
     */
    COMPLETED,
    
    /**
     * 失败状态 - 节点执行失败
     */
    FAILED,
    
    /**
     * 已跳过状态 - 节点被跳过执行
     */
    SKIPPED,
    
    /**
     * 已取消状态 - 节点被取消执行
     */
    CANCELLED
}