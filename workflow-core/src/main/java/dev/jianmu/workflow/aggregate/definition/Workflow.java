package dev.jianmu.workflow.aggregate.definition;

import dev.jianmu.workflow.aggregate.parameter.Parameter;
import dev.jianmu.workflow.el.EvaluationContext;
import dev.jianmu.workflow.el.EvaluationResult;
import dev.jianmu.workflow.el.Expression;
import dev.jianmu.workflow.el.ExpressionLanguage;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @program: workflow
 * @description: 流程定义实体
 * @author: Ethan Liu
 * @create: 2021-01-21 14:10
 **/
public class Workflow {
    public enum Type {
        WORKFLOW,
        PIPELINE
    }

    // 显示名称
    private String name;
    // 唯一引用名称
    private String ref;
    // 类型
    private Type type;
    // 描述
    private String description;
    // 版本
    private final String version = UUID.randomUUID().toString().replace("-", "");
    // Node列表
    private Set<Node> nodes;
    // 全局参数
    private Set<GlobalParameter> globalParameters = Set.of();
    // DSL原始内容
    private String dslText;
    // 表达式计算服务
    private ExpressionLanguage expressionLanguage;
    // 参数上下文
    private EvaluationContext context;

    private Workflow() {
    }

    public void setExpressionLanguage(ExpressionLanguage expressionLanguage) {
        this.expressionLanguage = expressionLanguage;
    }

    public void setContext(EvaluationContext context) {
        this.context = context;
    }

    public static Set<GlobalParameter> createGlobalParameters(Map<String, String> param) {
        return param.entrySet().stream().map(entry ->
                GlobalParameter.Builder.aGlobalParameter()
                        .name(entry.getKey())
                        .value(entry.getValue())
                        .build()
        ).collect(Collectors.toSet());
    }

    public Map<String, Parameter<?>> calculateTaskParams(String taskRef) {
        var asyncTask = this.findTask(taskRef);
        return asyncTask.getTaskParameters().stream().map(taskParameter -> {
            var parameter = this.calculateTaskParameter(taskParameter);
            return Map.entry(taskParameter.getRef(), parameter);
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Parameter<?> calculateTaskParameter(TaskParameter taskParameter) {
        // 密钥类型单独处理
        var secret = this.findSecret(taskParameter.getExpression());
        if (secret != null) {
            return Parameter.Type.SECRET.newParameter(secret);
        }
        String el;
        if (isEl(taskParameter.getExpression())) {
            el = taskParameter.getExpression();
        } else {
            el = "`" + taskParameter.getExpression() + "`";
        }
        // 计算参数表达式
        Expression expression = expressionLanguage.parseExpression(el);
        EvaluationResult evaluationResult = expressionLanguage.evaluateExpression(expression, context);
        if (evaluationResult.isFailure()) {
            var errorMsg = "参数：" + taskParameter.getRef() +
                    " 表达式: " + taskParameter.getExpression() +
                    " 计算错误: " + evaluationResult.getFailureMessage();
            throw new RuntimeException(errorMsg);
        }
        return evaluationResult.getValue();
    }

    private boolean isEl(String paramValue) {
        Pattern pattern = Pattern.compile("^\\(");
        Matcher matcher = pattern.matcher(paramValue);
        return matcher.lookingAt();
    }

    private String findSecret(String paramValue) {
        Pattern pattern = Pattern.compile("^\\(\\(([a-zA-Z0-9_-]+\\.*[a-zA-Z0-9_-]+)\\)\\)$");
        Matcher matcher = pattern.matcher(paramValue);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private AsyncTask findTask(String taskRef) {
        var node = this.findNode(taskRef);
        if (node instanceof AsyncTask) {
            return (AsyncTask) node;
        }
        throw new RuntimeException("未找到该异步任务：" + taskRef);
    }

    public Node findStart() {
        return this.nodes.stream()
                .filter(n -> n instanceof Start)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("未找到启动节点"));
    }

    public Node findEnd() {
        return this.nodes.stream()
                .filter(n -> n instanceof End)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("未找到结束节点"));
    }

    public Node findNode(String nodeRef) {
        return this.nodes.stream()
                .filter(n -> n.getRef().equals(nodeRef))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("未找到该节点定义: " + nodeRef));
    }

    // 返回当前节点上游Task的ref List
    public List<String> findTasks(String nodeRef) {
        Node node = this.findNode(nodeRef);
        return this.nodes.stream()
                .filter(n -> n instanceof AsyncTask)
                .map(Node::getRef)
                .filter(taskRef -> node.getSources().contains(taskRef))
                .collect(Collectors.toList());
    }

    // 返回AsyncTask列表
    public List<Node> findTasks() {
        return this.nodes.stream()
                .filter(n -> n instanceof AsyncTask)
                .collect(Collectors.toList());
    }

    public String getName() {
        return name;
    }

    public String getRef() {
        return ref;
    }

    public Type getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getVersion() {
        return version;
    }

    public Set<Node> getNodes() {
        return Set.copyOf(nodes);
    }

    public Set<GlobalParameter> getGlobalParameters() {
        return globalParameters;
    }

    public String getDslText() {
        return dslText;
    }

    public static final class Builder {
        // 显示名称
        private String name;
        // 唯一引用名称
        private String ref;
        // 类型
        private Type type;
        // 描述
        private String description;
        // Node列表
        private Set<Node> nodes;
        // 全局参数
        private Set<GlobalParameter> globalParameters;
        // DSL原始内容
        private String dslText;

        private Builder() {
        }

        public static Builder aWorkflow() {
            return new Builder();
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(Type type) {
            this.type = type;
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

        public Builder nodes(Set<Node> nodes) {
            this.nodes = nodes;
            return this;
        }

        public Builder globalParameters(Set<GlobalParameter> globalParameters) {
            this.globalParameters = globalParameters;
            return this;
        }

        public Builder dslText(String dslText) {
            this.dslText = dslText;
            return this;
        }

        public Workflow build() {

            // 添加业务规则检查
            if (this.nodes.size() < 2) {
                throw new RuntimeException("Node数量不能小于2");
            }
            long startCount = this.nodes.stream().filter(node -> node instanceof Start).count();
            if (startCount > 1) {
                throw new RuntimeException("开始节点不能多于1个");
            }
            long endCount = this.nodes.stream().filter(node -> node instanceof End).count();
            if (endCount > 1) {
                throw new RuntimeException("结束节点不能多于1个");
            }

            boolean d = this.nodes.stream()
                    .collect(Collectors.groupingBy(Node::getRef, Collectors.counting()))
                    .values().stream()
                    .anyMatch(count -> count > 1);
            if (d) {
                throw new RuntimeException("节点唯一引用名称不允许重复");
            }


            Workflow workflow = new Workflow();
            workflow.nodes = Set.copyOf(this.nodes);
            workflow.globalParameters = Set.copyOf(this.globalParameters);
            workflow.ref = this.ref;
            workflow.dslText = this.dslText;
            workflow.type = this.type;
            workflow.name = this.name;
            workflow.description = this.description;
            return workflow;
        }
    }
}
