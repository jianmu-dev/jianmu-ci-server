package dev.jianmu.infrastructure.mybatis.hub;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import dev.jianmu.hub.intergration.aggregate.NodeDefinition;
import dev.jianmu.hub.intergration.repository.NodeDefinitionRepository;
import dev.jianmu.infrastructure.mapper.hub.NodeDefinitionMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @class: NodeDefinitionRepositoryImpl
 * @description: NodeDefinitionRepositoryImpl
 * @author: Ethan Liu
 * @create: 2021-09-08 21:51
 **/
@Repository
public class NodeDefinitionRepositoryImpl implements NodeDefinitionRepository {
    private final NodeDefinitionMapper nodeDefinitionMapper;

    public NodeDefinitionRepositoryImpl(NodeDefinitionMapper nodeDefinitionMapper) {
        this.nodeDefinitionMapper = nodeDefinitionMapper;
    }

    @Override
    public Optional<NodeDefinition> findById(String id) {
        return this.nodeDefinitionMapper.findById(id);
    }


    public PageInfo<NodeDefinition> findPage(int pageNum, int pageSize) {
        return PageHelper.startPage(pageNum, pageSize)
                .doSelectPageInfo(this.nodeDefinitionMapper::findPage);
    }

    @Override
    public void saveOrUpdate(NodeDefinition nodeDefinition) {
        this.nodeDefinitionMapper.saveOrUpdate(nodeDefinition);
    }

    @Override
    public void deleteById(String id) {
        this.nodeDefinitionMapper.deleteById(id);
    }
}
