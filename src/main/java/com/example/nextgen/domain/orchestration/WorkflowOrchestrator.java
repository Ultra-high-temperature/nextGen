package com.example.nextgen.domain.orchestration;

import com.example.nextgen.domain.node.WorkflowNode;
import com.example.nextgen.domain.workflow.Workflow;

import java.util.List;
import java.util.Map;

/**
 * 工作流编排器接口
 * 负责定义节点执行顺序的编排策略
 */
public interface WorkflowOrchestrator {
    
    /**
     * 获取下一个可执行的节点列表
     * 
     * @param workflow 工作流实例
     * @return 可执行的节点列表
     */
    List<WorkflowNode> getNextExecutableNodes(Workflow workflow);
    
    /**
     * 检查节点是否可以执行
     * 
     * @param node 要检查的节点
     * @param workflow 工作流实例
     * @return 是否可以执行
     */
    boolean canExecuteNode(WorkflowNode node, Workflow workflow);
    
    /**
     * 获取节点的执行优先级
     * 
     * @param node 节点
     * @param workflow 工作流实例
     * @return 优先级（数值越小优先级越高）
     */
    int getNodePriority(WorkflowNode node, Workflow workflow);
    
    /**
     * 动态调整节点执行顺序
     * 
     * @param workflow 工作流实例
     * @param completedNode 刚完成的节点
     * @param executionResult 执行结果
     */
    void adjustExecutionOrder(Workflow workflow, WorkflowNode completedNode, Map<String, Object> executionResult);
    
    /**
     * 检查工作流是否已完成
     * 
     * @param workflow 工作流实例
     * @return 是否已完成
     */
    boolean isWorkflowCompleted(Workflow workflow);
    
    /**
     * 获取编排器类型
     * 
     * @return 编排器类型
     */
    OrchestrationType getOrchestrationType();
    
    /**
     * 验证工作流结构是否有效
     * 
     * @param workflow 工作流实例
     * @return 验证结果
     */
    ValidationResult validateWorkflowStructure(Workflow workflow);
    
    /**
     * 编排器类型枚举
     */
    enum OrchestrationType {
        STATIC,     // 静态编排
        DYNAMIC,    // 动态编排
        HYBRID      // 混合编排
    }
    
    /**
     * 验证结果
     */
    class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        
        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
        
        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }
        
        public static ValidationResult failure(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}