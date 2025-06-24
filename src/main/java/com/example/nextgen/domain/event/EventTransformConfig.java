//package com.example.nextgen.domain.event;
//
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.stereotype.Component;
//
//import java.util.HashSet;
//import java.util.Set;
//
///**
// * 事件转换配置
// * 用于控制哪些事件需要转换为前端展示内容
// */
//@Component
//@ConfigurationProperties(prefix = "workflow.event.transform")
//public class EventTransformConfig {
//
//    /**
//     * 需要转换的事件类型集合
//     */
//    private Set<String> enabledEventTypes = new HashSet<>();
//
//    /**
//     * 是否显示节点级别的事件
//     */
//    private boolean showNodeEvents = true;
//
//    /**
//     * 是否显示Agent级别的事件
//     */
//    private boolean showAgentEvents = false;
//
//    /**
//     * 是否显示任务提示内容
//     */
//    private boolean showTaskPrompts = false;
//
//    /**
//     * 是否显示Agent执行结果
//     */
//    private boolean showAgentResults = true;
//
//    /**
//     * 事件过滤级别
//     * ALL: 显示所有事件
//     * WORKFLOW_ONLY: 只显示工作流级别事件
//     * IMPORTANT_ONLY: 只显示重要事件
//     */
//    private FilterLevel filterLevel = FilterLevel.ALL;
//
//    /**
//     * 最大事件缓存数量
//     */
//    private int maxEventCache = 1000;
//
//    public EventTransformConfig() {
//        // 默认启用的事件类型
//        enabledEventTypes.add("WorkflowStarted");
//        enabledEventTypes.add("WorkflowCompleted");
//        enabledEventTypes.add("WorkflowFailed");
//        enabledEventTypes.add("NodeStarted");
//        enabledEventTypes.add("NodeCompleted");
//        enabledEventTypes.add("NodeFailed");
//    }
//
//    /**
//     * 检查是否应该转换指定的事件类型
//     */
//    public boolean shouldTransformEvent(String eventType) {
//        switch (filterLevel) {
//            case WORKFLOW_ONLY:
//                return eventType.startsWith("Workflow");
//            case IMPORTANT_ONLY:
//                return isImportantEvent(eventType);
//            case ALL:
//            default:
//                return enabledEventTypes.contains(eventType);
//        }
//    }
//
//    /**
//     * 判断是否为重要事件
//     */
//    private boolean isImportantEvent(String eventType) {
//        return eventType.equals("WorkflowStarted") ||
//               eventType.equals("WorkflowCompleted") ||
//               eventType.equals("WorkflowFailed") ||
//               eventType.equals("NodeFailed");
//    }
//
//    /**
//     * 添加启用的事件类型
//     */
//    public void addEnabledEventType(String eventType) {
//        this.enabledEventTypes.add(eventType);
//    }
//
//    /**
//     * 移除启用的事件类型
//     */
//    public void removeEnabledEventType(String eventType) {
//        this.enabledEventTypes.remove(eventType);
//    }
//
//    public enum FilterLevel {
//        ALL,
//        WORKFLOW_ONLY,
//        IMPORTANT_ONLY
//    }
//
//    // Getters and Setters
//    public Set<String> getEnabledEventTypes() {
//        return new HashSet<>(enabledEventTypes);
//    }
//
//    public void setEnabledEventTypes(Set<String> enabledEventTypes) {
//        this.enabledEventTypes = new HashSet<>(enabledEventTypes);
//    }
//
//    public boolean shouldShowNodeEvents() {
//        return showNodeEvents;
//    }
//
//    public void setShowNodeEvents(boolean showNodeEvents) {
//        this.showNodeEvents = showNodeEvents;
//    }
//
//    public boolean shouldShowAgentEvents() {
//        return showAgentEvents;
//    }
//
//    public void setShowAgentEvents(boolean showAgentEvents) {
//        this.showAgentEvents = showAgentEvents;
//    }
//
//    public boolean shouldShowTaskPrompts() {
//        return showTaskPrompts;
//    }
//
//    public void setShowTaskPrompts(boolean showTaskPrompts) {
//        this.showTaskPrompts = showTaskPrompts;
//    }
//
//    public boolean shouldShowAgentResults() {
//        return showAgentResults;
//    }
//
//    public void setShowAgentResults(boolean showAgentResults) {
//        this.showAgentResults = showAgentResults;
//    }
//
//    public FilterLevel getFilterLevel() {
//        return filterLevel;
//    }
//
//    public void setFilterLevel(FilterLevel filterLevel) {
//        this.filterLevel = filterLevel;
//    }
//
//    public int getMaxEventCache() {
//        return maxEventCache;
//    }
//
//    public void setMaxEventCache(int maxEventCache) {
//        this.maxEventCache = maxEventCache;
//    }
//}