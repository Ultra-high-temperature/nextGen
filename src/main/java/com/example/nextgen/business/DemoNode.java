package com.example.nextgen.business;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.example.nextgen.domain.node.NodeId;
import com.example.nextgen.domain.node.NodeType;
import com.example.nextgen.domain.node.WorkflowNode;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Map;
import java.util.Objects;

public class DemoNode extends WorkflowNode<Map<String, Object>, Map<String, Object>> {

    private ChatClient chatClient;

    public DemoNode(NodeId nodeId, String name, ChatClient chatClient) {
        super(nodeId, name, NodeType.CUSTOM);
        this.chatClient = chatClient;
    }

    @Override
    protected Map<String, Object> parseInputObject(String inputJson) {
        return JSON.parseObject(inputJson, new TypeReference<Map<String, Object>>() {
        });
    }

    @Override
    protected Map<String, Object> execute(Map<String, Object> params) {
        String text = Objects.requireNonNull(chatClient.prompt("你好").call().chatResponse()).getResult().getOutput().getText();
        return Map.of("userInput", text);
    }

}
