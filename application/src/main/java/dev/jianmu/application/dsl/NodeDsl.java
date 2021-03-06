package dev.jianmu.application.dsl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.jianmu.embedded.worker.aggregate.spec.ContainerSpec;
import dev.jianmu.hub.intergration.aggregate.NodeParameter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * @class: NodeDslVo
 * @description: 节点定义版本
 * @author: Ethan Liu
 * @create: 2021-10-01 13:12
 **/
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class NodeDsl {
    /**
     * 归属ref
     */
    private String ownerRef;

    /**
     * ref
     */
    private String ref;

    /**
     * 版本
     */
    private String version;

    /**
     * 返回文件路径
     */
    private String resultFile;

    /**
     * 输入参数
     */
    private Set<NodeParameter> inputParameters = new HashSet<>();

    /**
     * 输出参数
     */
    private Set<NodeParameter> outputParameters = new HashSet<>();

    /**
     * 镜像规格信息
     */
    private ContainerSpec spec;

    /**
     * 解析dsl
     *
     * @param dsl
     * @return
     */
    public static NodeDsl parseDsl(String dsl) {
        try {
            var mapper = new ObjectMapper(new YAMLFactory());
            var dslVo = mapper.readValue(dsl, NodeDsl.class);
            if (dslVo.ref.contains("/")) {
                var arr = dslVo.ref.split("/");
                dslVo.ownerRef = arr[0];
                dslVo.ref = arr[1];
            } else {
                dslVo.ownerRef = "local";
            }
            dslVo.checkRef();
            return dslVo;
        } catch (JsonProcessingException e) {
            log.error("e:", e);
            throw new RuntimeException("节点定义DSL格式不正确");
        }
    }

    /**
     * 校验ref
     */
    private void checkRef() {
        if (ObjectUtils.isEmpty(this.ref)) {
            throw new RuntimeException("节点定义DSL格式不正确");
        }
        RefChecker.check(this.ref);
    }

    public String getSpecString() {
        var mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this.spec);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("无法序列化Spec");
        }
    }
}
