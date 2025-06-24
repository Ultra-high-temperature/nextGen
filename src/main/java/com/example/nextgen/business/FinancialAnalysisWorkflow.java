package com.example.nextgen.business;

import com.alibaba.fastjson2.JSON;
import com.example.nextgen.domain.application.WorkflowApplicationService;
import com.example.nextgen.domain.application.WorkflowResults;
import com.example.nextgen.domain.node.EndNode;
import com.example.nextgen.domain.node.NodeId;
import com.example.nextgen.domain.node.NodeType;
import com.example.nextgen.domain.node.StartNode;
import com.example.nextgen.domain.orchestration.WorkflowOrchestrator;
import com.example.nextgen.domain.workflow.WorkflowStatus;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class FinancialAnalysisWorkflow {

    public FinancialAnalysisWorkflow(WorkflowApplicationService workflowService, ChatModel chatModel) {
        this.workflowService = workflowService;
        this.chatModel = chatModel;
    }

    private final WorkflowApplicationService workflowService;

    private final ChatModel chatModel;

    /**
     * 创建财报分析工作流
     * 流程：开始 -> 数据准备 -> (并行：图像处理 + 文本分析) -> 结果聚合 -> 结束
     */
    public String createAndExecuteWorkflow() {
        WorkflowApplicationService.CreateWorkflowCommand command = new WorkflowApplicationService.CreateWorkflowCommand();
        command.setName("并行处理工作流");
        command.setDescription("演示并行处理和结果聚合的工作流");
        command.setOrchestrationType(WorkflowOrchestrator.OrchestrationType.STATIC);

        // 创建节点
        List<WorkflowApplicationService.CreateNodeCommand> nodes = Arrays.asList(
                createStartNode(),
                createBusinessNode(),
                createEndNode());

        command.setNodes(nodes);

        // 创建边连接节点
        List<WorkflowApplicationService.CreateEdgeCommand> edges = Arrays.asList(
                createEdge("开始", "业务处理", "开始到业务", "SEQUENCE"),
                createEdge("业务处理", "结束", "业务到结束", "SEQUENCE"));

        command.setEdges(edges);

        WorkflowResults.WorkflowCreationResult result = workflowService.createWorkflow(command);

        WorkflowApplicationService.ExecuteWorkflowCommand executeCommand = new WorkflowApplicationService.ExecuteWorkflowCommand();
        executeCommand.setWorkflowId(result.getWorkflowId());
        workflowService.executeWorkflow(executeCommand);

        while (!workflowService.getWorkflowStatus(result.getWorkflowId()).getStatus().equals(WorkflowStatus.COMPLETED)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        WorkflowResults.WorkflowStatusResult workflowStatus = workflowService.getWorkflowStatus(result.getWorkflowId());
        String jsonString = JSON.toJSONString(workflowStatus.getGlobalContext());

        return jsonString;
    }

    /**
     * 创建边的辅助方法
     */
    private WorkflowApplicationService.CreateEdgeCommand createEdge(String sourceNodeName, String targetNodeName, String name, String type) {
        WorkflowApplicationService.CreateEdgeCommand edge = new WorkflowApplicationService.CreateEdgeCommand();
        edge.setSourceNodeName(sourceNodeName);
        edge.setTargetNodeName(targetNodeName);
        edge.setName(name);
        edge.setType(type);
        edge.setPriority(1);
        edge.setEnabled(true);
        return edge;
    }

    // 私有辅助方法 - 创建各种类型的节点

    private WorkflowApplicationService.CreateNodeCommand createStartNode() {
        WorkflowApplicationService.CreateNodeCommand node = new WorkflowApplicationService.CreateNodeCommand();
        node.setName("开始");
        node.setType(NodeType.START);
        node.setDependencies(Arrays.asList());
        node.setNodeSupplier(() -> new StartNode(NodeId.generate(), node.getName()));
        return node;
    }

    private WorkflowApplicationService.CreateNodeCommand createEndNode() {
        WorkflowApplicationService.CreateNodeCommand node = new WorkflowApplicationService.CreateNodeCommand();
        node.setName("结束");
        node.setType(NodeType.END);
        node.setDependencies(Arrays.asList("业务处理"));
        node.setNodeSupplier(() -> new EndNode(NodeId.generate(), node.getName()));
        return node;
    }


    private WorkflowApplicationService.CreateNodeCommand createBusinessNode() {
        WorkflowApplicationService.CreateNodeCommand node = new WorkflowApplicationService.CreateNodeCommand();
        node.setName("业务处理");
        node.setType(NodeType.CUSTOM);
        node.setDependencies(Arrays.asList("开始"));
        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(SimpleLoggerAdvisor.builder().build())
                .build();

        node.setNodeSupplier(() -> new DemoNode(NodeId.generate(), "业务处理", chatClient));
        return node;
    }


}
