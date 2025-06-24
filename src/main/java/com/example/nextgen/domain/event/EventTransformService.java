//package com.example.nextgen.domain.event;
//
//import org.example.llmdemo.workflow.domain.event.WorkflowEvent;
//import org.springframework.stereotype.Service;
//
///**
// * 事件转换服务
// * 根据业务配置将领域事件转换为前端关注的展示内容
// */
//@Service
//public class EventTransformService {
//
//    private final EventTransformConfig transformConfig;
//
//    public EventTransformService(EventTransformConfig transformConfig) {
//        this.transformConfig = transformConfig;
//    }
//
//    /**
//     * 将领域事件转换为前端事件
//     */
//    public FrontendEvent transformToFrontendEvent(WorkflowEvent domainEvent,
//                                                 WorkflowExecutionContext context) {
//
//        // 检查是否需要转换此事件类型
//        if (!transformConfig.shouldTransformEvent(domainEvent.getEventType())) {
//            return null;
//        }
//
//        switch (domainEvent.getEventType()) {
//            case "WorkflowStarted":
//                return transformWorkflowStarted((WorkflowEvent.WorkflowStarted) domainEvent, context);
//            case "WorkflowCompleted":
//                return transformWorkflowCompleted((WorkflowEvent.WorkflowCompleted) domainEvent, context);
//            case "WorkflowFailed":
//                return transformWorkflowFailed((WorkflowEvent.WorkflowFailed) domainEvent, context);
//            case "NodeStarted":
//                return transformNodeStarted((WorkflowEvent.NodeStarted) domainEvent, context);
//            case "NodeCompleted":
//                return transformNodeCompleted((WorkflowEvent.NodeCompleted) domainEvent, context);
//            case "NodeFailed":
//                return transformNodeFailed((WorkflowEvent.NodeFailed) domainEvent, context);
//            case "AgentExecutionStarted":
//                return transformAgentExecutionStarted((WorkflowEvent.AgentExecutionStarted) domainEvent, context);
//            case "AgentExecutionCompleted":
//                return transformAgentExecutionCompleted((WorkflowEvent.AgentExecutionCompleted) domainEvent, context);
//            default:
//                return null;
//        }
//    }
//
//    private FrontendEvent transformWorkflowStarted(WorkflowEvent.WorkflowStarted event,
//                                                  WorkflowExecutionContext context) {
//        return FrontendEvent.builder()
//            .eventId(event.getEventId())
//            .eventType("workflow_started")
//            .workflowId(event.getWorkflowId().getValue())
//            .title("工作流已启动")
//            .description("工作流 '" + event.getWorkflowName() + "' 开始执行")
//            .status("running")
//            .progress(0.0)
//            .timestamp(event.getOccurredAt())
//            .category("workflow")
//            .priority("high")
//            .addData("workflowName", event.getWorkflowName())
//            .build();
//    }
//
//    private FrontendEvent transformWorkflowCompleted(WorkflowEvent.WorkflowCompleted event,
//                                                    WorkflowExecutionContext context) {
//        return FrontendEvent.builder()
//            .eventId(event.getEventId())
//            .eventType("workflow_completed")
//            .workflowId(event.getWorkflowId().getValue())
//            .title("工作流已完成")
//            .description("工作流 '" + event.getWorkflowName() + "' 成功完成")
//            .status("completed")
//            .progress(1.0)
//            .timestamp(event.getOccurredAt())
//            .category("workflow")
//            .priority("high")
//            .addData("workflowName", event.getWorkflowName())
//            .addData("duration", context != null ? context.getDurationInSeconds() : 0)
//            .addData("finalOutput", event.getFinalOutput())
//            .build();
//    }
//
//    private FrontendEvent transformWorkflowFailed(WorkflowEvent.WorkflowFailed event,
//                                                 WorkflowExecutionContext context) {
//        return FrontendEvent.builder()
//            .eventId(event.getEventId())
//            .eventType("workflow_failed")
//            .workflowId(event.getWorkflowId().getValue())
//            .title("工作流执行失败")
//            .description("工作流 '" + event.getWorkflowName() + "' 执行失败: " + event.getErrorMessage())
//            .status("failed")
//            .progress(context != null ? context.getProgress() : 0.0)
//            .timestamp(event.getOccurredAt())
//            .category("workflow")
//            .priority("critical")
//            .addData("workflowName", event.getWorkflowName())
//            .addData("errorMessage", event.getErrorMessage())
//            .addData("duration", context != null ? context.getDurationInSeconds() : 0)
//            .build();
//    }
//
//    private FrontendEvent transformNodeStarted(WorkflowEvent.NodeStarted event,
//                                              WorkflowExecutionContext context) {
//        if (!transformConfig.shouldShowNodeEvents()) {
//            return null;
//        }
//
//        return FrontendEvent.builder()
//            .eventId(event.getEventId())
//            .eventType("node_started")
//            .workflowId(context != null ? context.getWorkflowId() : "unknown")
//            .title("节点开始执行")
//            .description("节点 '" + event.getNodeName() + "' 开始执行")
//            .status("running")
//            .progress(context != null ? context.getProgress() : 0.0)
//            .timestamp(event.getOccurredAt())
//            .category("node")
//            .priority("medium")
//            .addData("nodeId", event.getNodeId().getValue())
//            .addData("nodeName", event.getNodeName())
//            .build();
//    }
//
//    private FrontendEvent transformNodeCompleted(WorkflowEvent.NodeCompleted event,
//                                                WorkflowExecutionContext context) {
//        if (!transformConfig.shouldShowNodeEvents()) {
//            return null;
//        }
//
//        return FrontendEvent.builder()
//            .eventId(event.getEventId())
//            .eventType("node_completed")
//            .workflowId(context != null ? context.getWorkflowId() : "unknown")
//            .title("节点执行完成")
//            .description("节点 '" + event.getNodeName() + "' 执行完成")
//            .status("completed")
//            .progress(context != null ? context.getProgress() : 0.0)
//            .timestamp(event.getOccurredAt())
//            .category("node")
//            .priority("medium")
//            .addData("nodeId", event.getNodeId().getValue())
//            .addData("nodeName", event.getNodeName())
//            .addData("output", event.getOutput())
//            .build();
//    }
//
//    private FrontendEvent transformNodeFailed(WorkflowEvent.NodeFailed event,
//                                             WorkflowExecutionContext context) {
//        return FrontendEvent.builder()
//            .eventId(event.getEventId())
//            .eventType("node_failed")
//            .workflowId(context != null ? context.getWorkflowId() : "unknown")
//            .title("节点执行失败")
//            .description("节点 '" + event.getNodeName() + "' 执行失败: " + event.getErrorMessage())
//            .status("failed")
//            .progress(context != null ? context.getProgress() : 0.0)
//            .timestamp(event.getOccurredAt())
//            .category("node")
//            .priority("high")
//            .addData("nodeId", event.getNodeId().getValue())
//            .addData("nodeName", event.getNodeName())
//            .addData("errorMessage", event.getErrorMessage())
//            .build();
//    }
//
//    private FrontendEvent transformAgentExecutionStarted(WorkflowEvent.AgentExecutionStarted event,
//                                                        WorkflowExecutionContext context) {
//        if (!transformConfig.shouldShowAgentEvents()) {
//            return null;
//        }
//
//        return FrontendEvent.builder()
//            .eventId(event.getEventId())
//            .eventType("agent_started")
//            .workflowId(context != null ? context.getWorkflowId() : "unknown")
//            .title("Agent开始执行")
//            .description("Agent '" + event.getAgentName() + "' 开始执行任务")
//            .status("running")
//            .progress(context != null ? context.getProgress() : 0.0)
//            .timestamp(event.getOccurredAt())
//            .category("agent")
//            .priority("low")
//            .addData("nodeId", event.getNodeId().getValue())
//            .addData("agentName", event.getAgentName())
//            .addData("taskPrompt", transformConfig.shouldShowTaskPrompts() ? event.getTaskPrompt() : "[隐藏]")
//            .build();
//    }
//
//    private FrontendEvent transformAgentExecutionCompleted(WorkflowEvent.AgentExecutionCompleted event,
//                                                          WorkflowExecutionContext context) {
//        if (!transformConfig.shouldShowAgentEvents()) {
//            return null;
//        }
//
//        return FrontendEvent.builder()
//            .eventId(event.getEventId())
//            .eventType("agent_completed")
//            .workflowId(context != null ? context.getWorkflowId() : "unknown")
//            .title("Agent执行完成")
//            .description("Agent '" + event.getAgentName() + "' 执行完成")
//            .status("completed")
//            .progress(context != null ? context.getProgress() : 0.0)
//            .timestamp(event.getOccurredAt())
//            .category("agent")
//            .priority("low")
//            .addData("nodeId", event.getNodeId().getValue())
//            .addData("agentName", event.getAgentName())
//            .addData("result", transformConfig.shouldShowAgentResults() ? event.getResult() : "[隐藏]")
//            .build();
//    }
//}