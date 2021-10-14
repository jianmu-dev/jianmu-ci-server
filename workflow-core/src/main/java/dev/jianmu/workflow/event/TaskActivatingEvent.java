package dev.jianmu.workflow.event;

/**
 * @program: workflow
 * @description: 任务激活事件
 * @author: Ethan Liu
 * @create: 2021-01-21 20:47
 **/
public class TaskActivatingEvent extends BaseEvent {

    private TaskActivatingEvent() {
    }


    public static final class Builder {
        // 流程定义唯一引用名称
        protected String workflowRef;
        // 流程定义版本
        protected String workflowVersion;
        // 流程实例ID
        protected String workflowInstanceId;
        // 触发器ID
        protected String triggerId;
        // 节点唯一引用名称
        protected String nodeRef;
        // 节点类型
        protected String nodeType;

        private Builder() {
        }

        public static Builder aTaskActivatingEvent() {
            return new Builder();
        }

        public Builder workflowRef(String workflowRef) {
            this.workflowRef = workflowRef;
            return this;
        }

        public Builder workflowVersion(String workflowVersion) {
            this.workflowVersion = workflowVersion;
            return this;
        }

        public Builder workflowInstanceId(String workflowInstanceId) {
            this.workflowInstanceId = workflowInstanceId;
            return this;
        }

        public Builder triggerId(String triggerId) {
            this.triggerId = triggerId;
            return this;
        }

        public Builder nodeRef(String nodeRef) {
            this.nodeRef = nodeRef;
            return this;
        }

        public Builder nodeType(String nodeType) {
            this.nodeType = nodeType;
            return this;
        }

        public TaskActivatingEvent build() {
            TaskActivatingEvent taskActivatingEvent = new TaskActivatingEvent();
            taskActivatingEvent.triggerId = this.triggerId;
            taskActivatingEvent.workflowRef = this.workflowRef;
            taskActivatingEvent.workflowVersion = this.workflowVersion;
            taskActivatingEvent.workflowInstanceId = this.workflowInstanceId;
            taskActivatingEvent.nodeRef = this.nodeRef;
            taskActivatingEvent.nodeType = this.nodeType;
            return taskActivatingEvent;
        }
    }
}
