package com.api.jira.apis.project.service;

import com.api.jira.apis.project.entity.ProjectEntity;
import com.api.jira.apis.project.mapper.ProjectMapper;
import com.api.jira.apis.project.model.ProjectDto;
import com.api.jira.apis.project.repository.ProjectRepository;
import com.api.jira.apis.user.entity.UserEntity;
import com.api.jira.apis.user.mapper.UserMapper;
import com.api.jira.apis.user.model.UserDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ProjectDbService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final UserMapper userMapper;

    public ProjectDbService(ProjectRepository projectRepository, ProjectMapper projectMapper, UserMapper userMapper) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
        this.userMapper = userMapper;
    }

    public ProjectDto saveProject(ProjectDto projectDto){
        if(!Objects.isNull(projectDto)){
            ProjectEntity projectEntity = projectMapper.toProjectEntity(projectDto);
            return projectMapper.toProjectDto(projectRepository.save(projectEntity));
        }
        else {
            throw new RuntimeException("Error saving the project.");
        }
    }
    public ProjectDto saveProject(ProjectEntity project){
        ProjectEntity savedProject = projectRepository.save(project);
        return projectMapper.toProjectDto(savedProject);
    }

    public Optional<ProjectEntity> findProjectById(Integer projectId){
        return projectRepository.getProjectByProjectId(projectId);
    }

    public ProjectDto getProjectById(Integer projectId){
        return projectMapper.toProjectDto(projectRepository.getProjectByProjectId(projectId).orElse(null));
    }

    public List<UserDto> getUsersTaggedToProject(Integer projectId){
        List<UserEntity> userEntities = projectRepository.getUsersTaggedToProject(projectId);
        return userMapper.toUserDtoList(userEntities);
    }
}
