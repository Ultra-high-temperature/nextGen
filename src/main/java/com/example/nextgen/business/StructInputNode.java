package com.example.nextgen.business;

import com.example.nextgen.domain.node.NodeId;
import com.example.nextgen.domain.node.NodeType;
import com.example.nextgen.domain.node.WorkflowNode;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class StructInputNode extends WorkflowNode<StructInputNode.StructOutNodeInput, Map<String, Object>> {

    private ChatClient chatClient;

    protected StructInputNode(NodeId nodeId, String name, NodeType type, ChatClient chatClient) {
        super(nodeId, name, type);
        this.chatClient = chatClient;
    }

    @Override
    protected Map<String, Object> execute(StructOutNodeInput params) {
        String text = Objects.requireNonNull(chatClient.prompt("你好").call().chatResponse())
                .getResult().getOutput().getText();
        return Map.of();
    }

    record StructOutNodeInput(String userInput, Date currentDate) {
    }
}
