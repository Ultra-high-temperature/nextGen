package com.example.nextgen.domain.workflow;

/**
 * 工作流状态枚举
 */
public enum WorkflowStatus {
    /**
     * 已创建 - 工作流已创建但未启动
     */
    CREATED,
    
    /**
     * 运行中 - 工作流正在执行
     */
    RUNNING,
    
    /**
     * 已暂停 - 工作流被暂停
     */
    PAUSED,
    
    /**
     * 已完成 - 工作流成功完成
     */
    COMPLETED,
    
    /**
     * 已失败 - 工作流执行失败
     */
    FAILED,
    
    /**
     * 已取消 - 工作流被取消
     */
    CANCELLED
}