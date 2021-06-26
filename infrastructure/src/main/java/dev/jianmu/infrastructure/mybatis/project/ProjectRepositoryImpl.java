package dev.jianmu.infrastructure.mybatis.project;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import dev.jianmu.infrastructure.mapper.project.ProjectMapper;
import dev.jianmu.project.aggregate.Project;
import dev.jianmu.project.repository.ProjectRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @class: DslReferenceRepositoryImpl
 * @description: DSL流程定义关联仓储实现
 * @author: Ethan Liu
 * @create: 2021-04-23 11:39
 **/
@Repository
public class ProjectRepositoryImpl implements ProjectRepository {
    private final ProjectMapper projectMapper;

    public ProjectRepositoryImpl(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    @Override
    public void add(Project project) {
        this.projectMapper.add(project);
    }

    @Override
    public void deleteByWorkflowRef(String workflowRef) {
        this.projectMapper.deleteByWorkflowRef(workflowRef);
    }

    @Override
    public void updateByWorkflowRef(Project project) {
        this.projectMapper.updateByWorkflowRef(project);
    }

    @Override
    public Optional<Project> findById(String id) {
        return this.projectMapper.findById(id);
    }

    @Override
    public Optional<Project> findByIdAndWebhook(String id, String webhook) {
        return this.projectMapper.findByIdAndWebhook(id, webhook);
    }

    @Override
    public Optional<Project> findByWorkflowRef(String workflowRef) {
        return this.projectMapper.findByWorkflowRef(workflowRef);
    }

    public PageInfo<Project> findAllPage(String workflowName, int pageNum, int pageSize) {
        return PageHelper.startPage(pageNum, pageSize)
                .doSelectPageInfo(() -> this.projectMapper.findAllPage(workflowName));
    }

    public List<Project> findAll() {
        return this.projectMapper.findAll();
    }
}
