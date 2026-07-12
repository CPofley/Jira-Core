package com.api.jira.apis.project.mapper;

import com.api.jira.apis.comment.mapper.CommentMapper;
import com.api.jira.apis.project.entity.ProjectEntity;
import com.api.jira.apis.project.model.ProjectDto;
import com.api.jira.sharedMapper.mapper.CommonMapper;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {CommonMapper.class})
public interface ProjectMapper {

    ProjectDto toProjectDto(ProjectEntity projectEntity);
    ProjectEntity toProjectEntity(ProjectDto projectDto);

    List<ProjectDto> toProjectDtoList(List<ProjectEntity> projectEntityList);
    List<ProjectEntity> toProjectEntityList(List<ProjectDto> projectDtoList);
}
