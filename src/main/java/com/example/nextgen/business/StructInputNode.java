package com.example.nextgen.business;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.example.nextgen.domain.node.NodeId;
import com.example.nextgen.domain.node.NodeType;
import com.example.nextgen.domain.node.WorkflowNode;
import org.springframework.ai.chat.client.ChatClient;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class StructInputNode extends WorkflowNode<StructInputNode.StructOutNodeInput, Map<String, Object>> {

    private ChatClient chatClient;

    protected StructInputNode(NodeId nodeId, String name,  ChatClient chatClient) {
        super(nodeId, name, NodeType.CUSTOM);
        this.chatClient = chatClient;
    }

    @Override
    protected StructOutNodeInput parseInputObject(String inputJson) {
        return JSON.parseObject(inputJson, new TypeReference<StructOutNodeInput>() {
        });
    }

    @Override
    protected Map<String, Object> execute(StructOutNodeInput params) {
        String text = Objects.requireNonNull(chatClient.prompt()
                        .system("你是一个尖锐的批评者，请毫不留情的批判任何用户输入")
                        .user(params.userInput())
                        .call().chatResponse())
                .getResult().getOutput().getText();
        return Map.of("text",text);
    }

    record StructOutNodeInput(@NotNull String userInput, Date currentDate) {
    }
}
