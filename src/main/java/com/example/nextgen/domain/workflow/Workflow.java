package com.example.nextgen.domain.workflow;

import com.example.nextgen.domain.edge.EdgeId;
import com.example.nextgen.domain.edge.EdgeType;
import com.example.nextgen.domain.edge.WorkflowEdge;
import com.example.nextgen.domain.event.WorkflowEvent;
import com.example.nextgen.domain.node.NodeId;
import com.example.nextgen.domain.node.NodeStatus;
import com.example.nextgen.domain.node.WorkflowNode;
import lombok.AllArgsConstructor; // 导入
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 工作流聚合根
 * 管理整个工作流的生命周期和节点编排
 */
@Slf4j
@Builder
@Getter
@AllArgsConstructor // <-- 1. 添加全参构造函数注解
/**
 * 工作流聚合根
 * 管理整个工作流的生命周期和节点编排
 */
public class Workflow {
    private final WorkflowId workflowId;
    private String name;
    private String description;

    @Builder.Default // <-- 3. 为需要默认值的字段添加注解
    private WorkflowStatus status = WorkflowStatus.CREATED;

    @Builder.Default
    private final Map<NodeId, WorkflowNode> nodes = new HashMap<>();

    @Builder.Default
    private final Map<EdgeId, WorkflowEdge> edges = new HashMap<>();

    @Builder.Default
    private final List<WorkflowEvent> domainEvents = new ArrayList<>();

    @Builder.Default
    private Map<String, Object> globalContext = new HashMap<>();

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    /**
     * 添加节点到工作流
     */
    public void addNode(WorkflowNode node) {
        Objects.requireNonNull(node, "Node cannot be null");
        if (status != WorkflowStatus.CREATED && status != WorkflowStatus.PAUSED) {
            throw new IllegalStateException("Cannot add nodes to a running or completed workflow");
        }
        nodes.put(node.getNodeId(), node);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 移除节点
     */
    public void removeNode(NodeId nodeId) {
        if (status != WorkflowStatus.CREATED && status != WorkflowStatus.PAUSED) {
            throw new IllegalStateException("Cannot remove nodes from a running or completed workflow");
        }
        // 移除节点时，同时移除相关的边
        removeEdgesForNode(nodeId);
        nodes.remove(nodeId);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 添加边到工作流
     */
    public void addEdge(WorkflowEdge edge) {
        Objects.requireNonNull(edge, "Edge cannot be null");
        if (status != WorkflowStatus.CREATED && status != WorkflowStatus.PAUSED) {
            throw new IllegalStateException("Cannot add edges to a running or completed workflow");
        }

        // 验证源节点和目标节点是否存在
        if (!nodes.containsKey(edge.getSourceNodeId())) {
            throw new IllegalArgumentException("Source node not found: " + edge.getSourceNodeId());
        }
        if (!nodes.containsKey(edge.getTargetNodeId())) {
            throw new IllegalArgumentException("Target node not found: " + edge.getTargetNodeId());
        }

        edges.put(edge.getId(), edge);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 批量添加边
     */
    public void addEdges(WorkflowEdge... edges) {
        for (WorkflowEdge edge : edges) {
            addEdge(edge);
        }
    }

    /**
     * 移除边
     */
    public void removeEdge(EdgeId edgeId) {
        if (status != WorkflowStatus.CREATED && status != WorkflowStatus.PAUSED) {
            throw new IllegalStateException("Cannot remove edges from a running or completed workflow");
        }
        edges.remove(edgeId);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 移除节点相关的所有边
     */
    private void removeEdgesForNode(NodeId nodeId) {
        List<EdgeId> edgesToRemove = new ArrayList<>();
        for (WorkflowEdge edge : edges.values()) {
            if (edge.getSourceNodeId().equals(nodeId) || edge.getTargetNodeId().equals(nodeId)) {
                edgesToRemove.add(edge.getId());
            }
        }
        edgesToRemove.forEach(edges::remove);
    }

    /**
     * 获取节点的输入边
     */
    public List<WorkflowEdge> getIncomingEdges(NodeId nodeId) {
        return edges.values().stream()
                .filter(edge -> edge.getTargetNodeId().equals(nodeId))
                .filter(WorkflowEdge::isEnabled)
                .sorted(Comparator.comparing(WorkflowEdge::getPriority))
                .collect(Collectors.toList());
    }

    /**
     * 获取节点的输出边
     */
    public List<WorkflowEdge> getOutgoingEdges(NodeId nodeId) {
        return edges.values().stream()
                .filter(edge -> edge.getSourceNodeId().equals(nodeId))
                .filter(WorkflowEdge::isEnabled)
                .sorted(Comparator.comparing(WorkflowEdge::getPriority))
                .collect(Collectors.toList());
    }

    /**
     * 获取满足条件的输出边
     */
    public List<WorkflowEdge> getConditionalOutgoingEdges(NodeId nodeId, Map<String, Object> context) {
        return getOutgoingEdges(nodeId).stream()
                .filter(edge -> edge.isConditionMet(context))
                .collect(Collectors.toList());
    }

    /**
     * 根据源节点和目标节点查找边
     */
    public List<WorkflowEdge> findEdgesBetween(NodeId sourceId, NodeId targetId) {
        return edges.values().stream()
                .filter(edge -> edge.getSourceNodeId().equals(sourceId) &&
                        edge.getTargetNodeId().equals(targetId))
                .collect(Collectors.toList());
    }

    /**
     * 获取指定类型的边
     */
    public List<WorkflowEdge> getEdgesByType(EdgeType type) {
        return edges.values().stream()
                .filter(edge -> edge.getType() == type)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有边
     */
    public Collection<WorkflowEdge> getEdges() {
        return Collections.unmodifiableCollection(edges.values());
    }

    /**
     * 获取边
     */
    public WorkflowEdge getEdge(EdgeId edgeId) {
        return edges.get(edgeId);
    }

    /**
     * 启动工作流
     */
    public void start() {
        if (status != WorkflowStatus.CREATED) {
            throw new IllegalStateException("Workflow can only be started from CREATED status");
        }
        if (nodes.isEmpty()) {
            throw new IllegalStateException("Cannot start workflow without nodes");
        }

        this.status = WorkflowStatus.RUNNING;
        this.startedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        addDomainEvent(new WorkflowEvent.WorkflowStarted(workflowId, name));

        // 启动所有没有依赖的节点
        startReadyNodes();
    }

    /**
     * 完成工作流
     */
    public void complete(Map<String, Object> finalOutput) {
        if (status != WorkflowStatus.RUNNING) {
            throw new IllegalStateException("Workflow can only be completed from RUNNING status");
        }

        this.status = WorkflowStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (finalOutput != null) {
            this.globalContext.putAll(finalOutput);
        }

        addDomainEvent(new WorkflowEvent.WorkflowCompleted(workflowId, name, finalOutput));
    }

    /**
     * 工作流失败
     */
    public void fail(String errorMessage) {
        log.error("Workflow failed: " + errorMessage);
        if (status != WorkflowStatus.RUNNING) {
            throw new IllegalStateException("Workflow can only fail from RUNNING status");
        }

        this.status = WorkflowStatus.FAILED;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        addDomainEvent(new WorkflowEvent.WorkflowFailed(workflowId, name, errorMessage));
    }

    /**
     * 暂停工作流
     */
    public void pause() {
        if (status != WorkflowStatus.RUNNING) {
            throw new IllegalStateException("Workflow can only be paused from RUNNING status");
        }
        this.status = WorkflowStatus.PAUSED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 恢复工作流
     */
    public void resume() {
        if (status != WorkflowStatus.PAUSED) {
            throw new IllegalStateException("Workflow can only be resumed from PAUSED status");
        }
        this.status = WorkflowStatus.RUNNING;
        this.updatedAt = LocalDateTime.now();
        startReadyNodes();
    }

    /**
     * 节点完成后的处理
     */
    public void onNodeCompleted(NodeId nodeId) {
        WorkflowNode node = nodes.get(nodeId);
        if (node == null) {
            throw new IllegalArgumentException("Node not found: " + nodeId);
        }

        // 将节点输出添加到全局上下文
        globalContext.putAll(node.getOutputData());

        // 启动新的就绪节点
        startReadyNodes();

        // 检查工作流是否完成
        checkWorkflowCompletion();
    }

    /**
     * 节点失败后的处理
     */
    public void onNodeFailed(NodeId nodeId, String errorMessage) {
        // 根据失败策略处理
        // 这里简单实现为整个工作流失败
        fail("Node " + nodeId + " failed: " + errorMessage);
    }

    /**
     * 启动所有就绪的节点
     */
    private void startReadyNodes() {
        if (status != WorkflowStatus.RUNNING) {
            return;
        }

        Set<NodeId> completedNodes = getCompletedNodeIds();

        nodes.values().stream()
                .filter(node -> node.getStatus() == NodeStatus.IDLE)
                .filter(node -> node.areDependenciesSatisfied(completedNodes))
                .forEach(WorkflowNode::start);
    }

    /**
     * 检查工作流是否完成
     */
    private void checkWorkflowCompletion() {
        if (status != WorkflowStatus.RUNNING) {
            return;
        }

        boolean allCompleted = nodes.values().stream()
                .allMatch(node -> node.getStatus() == NodeStatus.COMPLETED ||
                        node.getStatus() == NodeStatus.SKIPPED);

        if (allCompleted) {
            complete(new HashMap<>(globalContext));
        }
    }

    /**
     * 获取已完成的节点ID集合
     */
    private Set<NodeId> getCompletedNodeIds() {
        return nodes.values().stream()
                .filter(node -> node.getStatus() == NodeStatus.COMPLETED)
                .map(WorkflowNode::getNodeId)
                .collect(Collectors.toSet());
    }

    /**
     * 获取可执行的节点列表
     */
    public List<WorkflowNode> getExecutableNodes() {
        if (status != WorkflowStatus.RUNNING) {
            return Collections.emptyList();
        }

        Set<NodeId> completedNodes = getCompletedNodeIds();

        return nodes.values().stream()
                .filter(node -> node.getStatus() == NodeStatus.IDLE)
                .filter(node -> node.areDependenciesSatisfied(completedNodes))
                .collect(Collectors.toList());
    }

    /**
     * 获取正在运行的节点列表
     */
    public List<WorkflowNode> getRunningNodes() {
        return nodes.values().stream()
                .filter(node -> node.getStatus() == NodeStatus.RUNNING)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取节点
     */
    public WorkflowNode getNode(NodeId nodeId) {
        return nodes.get(nodeId);
    }

    /**
     * 更新全局上下文
     */
    public void updateGlobalContext(String key, Object value) {
        this.globalContext.put(key, value);
        this.updatedAt = LocalDateTime.now();
    }

    private void addDomainEvent(WorkflowEvent event) {
        this.domainEvents.add(event);
    }

    public List<WorkflowEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        this.domainEvents.clear();
    }

    /**
     * 获取节点数量
     */
    public int getNodeCount() {
        return nodes.size();
    }

    /**
     * 获取边数量
     */
    public int getEdgeCount() {
        return edges.size();
    }

    /**
     * 检查是否包含节点
     */
    public boolean containsNode(NodeId nodeId) {
        return nodes.containsKey(nodeId);
    }

    /**
     * 检查是否包含边
     */
    public boolean containsEdge(EdgeId edgeId) {
        return edges.containsKey(edgeId);
    }

}