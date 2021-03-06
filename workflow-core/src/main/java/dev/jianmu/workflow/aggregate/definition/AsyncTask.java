package dev.jianmu.workflow.aggregate.definition;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @program: workflow
 * @description: 异步任务节点
 * @author: Ethan Liu
 * @create: 2021-01-21 20:42
 **/
public class AsyncTask extends BaseNode {

    private AsyncTask() {
    }

    public static Set<TaskParameter> createTaskParameters(Map<String, String> param) {
        return param.entrySet().stream().map(entry ->
                TaskParameter.Builder.aTaskParameter()
                        .ref(entry.getKey())
                        .expression(entry.getValue())
                        .build()
        ).collect(Collectors.toSet());
    }

    public static final class Builder {
        // 显示名称
        protected String name;
        // 唯一引用名称
        protected String ref;
        // 描述
        protected String description;
        // 类型
        protected String type;
        private Set<TaskParameter> taskParameters;

        private Builder() {
        }

        public static Builder anAsyncTask() {
            return new Builder();
        }

        public Builder taskParameters(Set<TaskParameter> taskParameters) {
            this.taskParameters = taskParameters;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder ref(String ref) {
            this.ref = ref;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public AsyncTask build() {
            AsyncTask asyncTask = new AsyncTask();
            asyncTask.name = this.name;
            asyncTask.ref = this.ref;
            asyncTask.description = this.description;
            asyncTask.taskParameters = this.taskParameters;
            asyncTask.type = this.type;
            return asyncTask;
        }
    }
}
