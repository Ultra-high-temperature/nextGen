package com.example.nextgen.domain.node;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 结束节点
 * 工作流的终点，负责收集和处理最终结果
 */
public class EndNode extends WorkflowNode<Map<String, Object>,Map<String, Object>> {
    
    private final Function<Map<String, Object>, Map<String, Object>> resultProcessor;
    private final boolean collectAllData; // 是否收集所有输入数据
    
    public EndNode(NodeId nodeId, String name) {
        this(nodeId, name, true, null);
    }

    public EndNode(NodeId nodeId, String name, boolean collectAllData, 
                   Function<Map<String, Object>, Map<String, Object>> resultProcessor) {
        super(nodeId, name, NodeType.END);
        this.collectAllData = collectAllData;
        this.resultProcessor = resultProcessor;
    }
    @Override
    protected Map<String, Object> parseInputObject(String inputJson) {
        return JSON.parseObject(inputJson, new TypeReference<Map<String, Object>>() {
        });
    }
    @Override
    protected Map<String, Object> execute(Map<String, Object> initialData) {
        Map<String, Object> output = new HashMap<>();

        try {

            // 根据配置决定是否收集所有输入数据
            if (collectAllData) {
                output.putAll(getInputData());
            }
            
            // 添加工作流结束的元数据
            output.put("workflowEndTime", System.currentTimeMillis());
            output.put("endNodeId", getNodeId().getValue());
            output.put("endNodeName", getName());
            
            // 计算执行时长（如果有开始时间）
            Object startTime = getInputData().get("workflowStartTime");
            if (startTime instanceof Long) {
                long duration = System.currentTimeMillis() - (Long) startTime;
                output.put("workflowDuration", duration);
            }
            
            // 如果有结果处理器，使用它处理最终结果
            if (resultProcessor != null) {
                try {
                    Map<String, Object> processedResult = resultProcessor.apply(new HashMap<>(output));
                    if (processedResult != null) {
                        output = processedResult;
                    }
                } catch (Exception e) {
                    // 结果处理失败，记录错误但不影响整体执行
                    output.put("resultProcessingError", e.getMessage());
                }
            }
            
            // 添加执行摘要
            addExecutionSummary(output);
            
            // 标记执行完成
//            complete(output);
            
        } catch (Exception e) {
            fail("结束节点执行失败: " + e.getMessage());
        }
        return output;
    }
    
    /**
     * 添加执行摘要信息
     */
    private void addExecutionSummary(Map<String, Object> output) {
        Map<String, Object> summary = new HashMap<>();
        
        // 统计数据项数量
        summary.put("totalDataItems", output.size());
        
        // 检查是否有错误信息
        boolean hasErrors = output.entrySet().stream()
            .anyMatch(entry -> entry.getKey().toLowerCase().contains("error") ||
                             entry.getKey().toLowerCase().contains("exception"));
        summary.put("hasErrors", hasErrors);
        
        // 检查执行状态
        summary.put("executionStatus", hasErrors ? "COMPLETED_WITH_ERRORS" : "COMPLETED_SUCCESSFULLY");
        
        output.put("executionSummary", summary);
    }
    
    /**
     * 是否收集所有数据
     */
    public boolean isCollectAllData() {
        return collectAllData;
    }
    
    /**
     * 是否有结果处理器
     */
    public boolean hasResultProcessor() {
        return resultProcessor != null;
    }
    
    /**
     * 验证节点配置是否有效
     * 结束节点总是有效的
     */
    public boolean isValid() {
        return true;
    }
    
    /**
     * 获取节点描述
     */
    public String getDescription() {
        return String.format("结束节点[%s] - 收集数据: %s, 结果处理器: %s", 
                           getName(), 
                           collectAllData ? "是" : "否",
                           hasResultProcessor() ? "是" : "否");
    }
    
    /**
     * 创建简单的结束节点
     */
    public static EndNode createSimple(NodeId nodeId, String name) {
        return new EndNode(nodeId, name, true, null);
    }
    
    /**
     * 创建带结果处理器的结束节点
     */
    public static EndNode createWithProcessor(NodeId nodeId, String name, 
                                             Function<Map<String, Object>, Map<String, Object>> processor) {
        return new EndNode(nodeId, name, true, processor);
    }
    
    /**
     * 创建不收集数据的结束节点
     */
    public static EndNode createMinimal(NodeId nodeId, String name) {
        return new EndNode(nodeId, name, false, null);
    }
}