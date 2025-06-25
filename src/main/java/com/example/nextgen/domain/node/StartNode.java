package com.example.nextgen.domain.node;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;

import java.util.HashMap;
import java.util.Map;

/**
 * 开始节点
 * 工作流的起始点，负责初始化工作流执行环境
 */
public class StartNode extends WorkflowNode<Map<String, Object>,Map<String, Object>> {
    
    private final Map<String, Object> initialData;
    
    public StartNode(NodeId nodeId, String name) {
        super(nodeId, name, NodeType.START);
        this.initialData = new HashMap<>();
    }


    @Override
    protected Map<String, Object> parseInputObject(String inputJson) {
        return JSON.parseObject(inputJson, new TypeReference<Map<String, Object>>() {
        });
    }

    @Override
    protected Map<String, Object> execute(Map<String, Object> initialData) {

        // 开始节点的执行逻辑很简单，主要是初始化数据
        Map<String, Object> output = new HashMap<>();
        try {
            
            // 合并初始数据和输入数据
            output.putAll(initialData);
            output.putAll(getInputData());
            
            // 添加工作流开始的元数据
            output.put("workflowStartTime", System.currentTimeMillis());
            output.put("startNodeId", getNodeId().getValue());
            output.put("startNodeName", getName());
            
            // 标记执行完成
//            complete(output);
            
        } catch (Exception e) {
            fail("开始节点执行失败: " + e.getMessage());
        }
        return output;
    }
    
    /**
     * 设置初始数据
     */
    public void setInitialData(String key, Object value) {
        this.initialData.put(key, value);
    }
    
    /**
     * 批量设置初始数据
     */
    public void setInitialData(Map<String, Object> data) {
        if (data != null) {
            this.initialData.putAll(data);
        }
    }
    
    /**
     * 获取初始数据
     */
    public Map<String, Object> getInitialData() {
        return new HashMap<>(initialData);
    }
    
    /**
     * 清除初始数据
     */
    public void clearInitialData() {
        this.initialData.clear();
    }
    
    /**
     * 验证节点配置是否有效
     * 开始节点总是有效的
     */
    public boolean isValid() {
        return true;
    }
    
    /**
     * 获取节点描述
     */
    public String getDescription() {
        return String.format("开始节点[%s] - 初始数据项: %d", 
                           getName(), initialData.size());
    }

}