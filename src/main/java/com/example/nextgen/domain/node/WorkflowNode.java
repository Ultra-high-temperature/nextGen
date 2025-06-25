package com.example.nextgen.domain.node;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.example.nextgen.domain.event.WorkflowEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 工作流节点聚合根
 * 代表工作流中的一个执行单元
 */
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public abstract class WorkflowNode<T, R> {
    private final NodeId nodeId;
    private String name;
    private NodeType type;
    private NodeStatus status = NodeStatus.IDLE;
    private Map<String, Object> inputData = new HashMap<>();
    private Map<String, Object> outputData = new HashMap<>();
    private String taskPrompt;
    private Set<NodeId> dependencies = new HashSet<>();
    private Map<String, Object> metadata = new HashMap<>();
    private List<WorkflowEvent> domainEvents = new ArrayList<>();
    private Date createdAt = new Date();
    private Date updatedAt = new Date();


    protected WorkflowNode(NodeId nodeId, String name, NodeType type) {
        this.nodeId = Objects.requireNonNull(nodeId, "NodeId cannot be null");
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.type = Objects.requireNonNull(type, "Type cannot be null");
        this.status = NodeStatus.IDLE;
        this.inputData = new HashMap<>();
        this.outputData = new HashMap<>();
        this.dependencies = new HashSet<>();
        this.metadata = new HashMap<>();
        this.domainEvents = new ArrayList<>();
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    /**
     * 启动节点执行
     */
    public void start() {
        if (status != NodeStatus.IDLE) {
            throw new IllegalStateException("Node can only be started from IDLE status");
        }
        this.status = NodeStatus.RUNNING;
        this.updatedAt = new Date();
        addDomainEvent(new WorkflowEvent.NodeStarted(nodeId, name));

        try {
            // 输入Map，在这里按实参匹配，将参数传入apply方法
            String jsonString = JSON.toJSONString(inputData);
            T javaBean = parseInputObject(jsonString);
            // 实际业务逻辑
            R apply = execute(javaBean);
            JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(apply));
            Map<String, Object> collect = jsonObject.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            outputData.putAll(collect);
        } catch (Exception e) {
            // todo 异常处理
            addDomainEvent(new WorkflowEvent.NodeFailed(nodeId, name, e.getMessage()));
        }
    }


    /**
     * 父类定义了何时解析，但将具体如何解析的实现留给子类。
     * @param inputJson 输入的JSON字符串
     * @return 解析后的 T 类型对象
     */
    protected abstract T parseInputObject(String inputJson);

    /**
     * 抽象执行方法，子类必须实现具体的业务逻辑
     * 执行完成后应调用 complete() 方法
     */
    protected abstract R execute(T params);

    /**
     * 完成节点执行
     */
    public void complete(Map<String, Object> input) {
        if (status != NodeStatus.RUNNING) {
            throw new IllegalStateException("Node can only be completed from RUNNING status");
        }
        outputData.putAll(input);
        this.status = NodeStatus.COMPLETED;
        this.outputData = Collections.unmodifiableMap(outputData);
        this.updatedAt = new Date();
        addDomainEvent(new WorkflowEvent.NodeCompleted(nodeId, name, outputData));
    }

    /**
     * 节点执行失败
     */
    public void fail(String errorMessage) {
        if (status != NodeStatus.RUNNING) {
            throw new IllegalStateException("Node can only fail from RUNNING status");
        }
        this.status = NodeStatus.FAILED;
        this.metadata.put("errorMessage", errorMessage);
        this.updatedAt = new Date();
        addDomainEvent(new WorkflowEvent.NodeFailed(nodeId, name, errorMessage));
    }

    /**
     * 重置节点状态
     */
    public void reset() {
        this.status = NodeStatus.IDLE;
        this.outputData.clear();
        this.metadata.remove("errorMessage");
        this.updatedAt = new Date();
        addDomainEvent(new WorkflowEvent.NodeReset(nodeId, name));
    }

    /**
     * 检查依赖是否满足
     */
    public boolean areDependenciesSatisfied(Set<NodeId> completedNodes) {
        return completedNodes.containsAll(dependencies);
    }

    /**
     * 添加依赖
     */
    public void addDependency(NodeId dependency) {
        this.dependencies.add(dependency);
        this.updatedAt = new Date();
    }

    /**
     * 设置输入数据
     */
    public void setInputData(Map<String, Object> inputData) {
        this.inputData = new HashMap<>(inputData != null ? inputData : Collections.emptyMap());
        this.updatedAt = new Date();
    }

    /**
     * 设置任务提示
     */
    public void setTaskPrompt(String taskPrompt) {
        this.taskPrompt = taskPrompt;
        this.updatedAt = new Date();
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
}