package com.example.nextgen.domain.edge;

/**
 * 边类型枚举
 * 定义工作流中不同类型的边
 */
public enum EdgeType {
    
    /**
     * 顺序边 - 普通的顺序执行连接
     */
    SEQUENCE("顺序边", "普通的顺序执行连接"),
    
    /**
     * 条件边 - 基于条件的连接
     */
    CONDITIONAL("条件边", "基于条件表达式的连接"),
    
    /**
     * 并行边 - 并行执行的连接
     */
    PARALLEL("并行边", "支持并行执行的连接"),
    
    /**
     * 数据流边 - 传递数据的连接
     */
    DATA_FLOW("数据流边", "用于传递数据的连接"),
    
    /**
     * 错误处理边 - 错误处理连接
     */
    ERROR_HANDLING("错误处理边", "用于错误处理的连接"),
    
    /**
     * 回退边 - 回退或重试连接
     */
    FALLBACK("回退边", "用于回退或重试的连接"),
    
    /**
     * 循环边 - 循环执行连接
     */
    LOOP("循环边", "用于循环执行的连接"),
    
    /**
     * 跳转边 - 跳过某些节点的连接
     */
    SKIP("跳转边", "用于跳过节点的连接"),
    
    /**
     * 聚合边 - 聚合多个输入的连接
     */
    AGGREGATION("聚合边", "用于聚合多个输入的连接"),
    
    /**
     * 分发边 - 分发到多个输出的连接
     */
    DISTRIBUTION("分发边", "用于分发到多个输出的连接");
    
    private final String displayName;
    private final String description;
    
    EdgeType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    /**
     * 获取显示名称
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 获取描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 检查是否为条件类型的边
     */
    public boolean isConditional() {
        return this == CONDITIONAL || this == ERROR_HANDLING || this == FALLBACK;
    }
    
    /**
     * 检查是否为并行类型的边
     */
    public boolean isParallel() {
        return this == PARALLEL || this == DISTRIBUTION;
    }
    
    /**
     * 检查是否为数据传递类型的边
     */
    public boolean isDataTransfer() {
        return this == DATA_FLOW || this == AGGREGATION;
    }
    
    /**
     * 检查是否为控制流类型的边
     */
    public boolean isControlFlow() {
        return this == SEQUENCE || this == CONDITIONAL || this == LOOP || this == SKIP;
    }
    
    /**
     * 根据名称获取边类型
     */
    public static EdgeType fromName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return SEQUENCE; // 默认类型
        }
        
        try {
            return EdgeType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 如果找不到匹配的类型，返回默认类型
            return SEQUENCE;
        }
    }
    
    /**
     * 获取所有条件类型的边
     */
    public static EdgeType[] getConditionalTypes() {
        return new EdgeType[]{CONDITIONAL, ERROR_HANDLING, FALLBACK};
    }
    
    /**
     * 获取所有并行类型的边
     */
    public static EdgeType[] getParallelTypes() {
        return new EdgeType[]{PARALLEL, DISTRIBUTION};
    }
    
    /**
     * 获取所有数据传递类型的边
     */
    public static EdgeType[] getDataTransferTypes() {
        return new EdgeType[]{DATA_FLOW, AGGREGATION};
    }
    
    @Override
    public String toString() {
        return displayName + " (" + name() + ")";
    }
}