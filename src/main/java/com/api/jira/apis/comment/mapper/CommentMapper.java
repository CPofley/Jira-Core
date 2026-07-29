package com.api.jira.apis.comment.mapper;

import com.api.jira.apis.comment.entity.CommentEntity;
import com.api.jira.apis.comment.models.CommentDto;
import com.api.jira.sharedMapper.mapper.CommonMapper; // 🛠️ Import shared mapper
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {CommonMapper.class}) // 🛠️ Added uses
public interface CommentMapper {

    @Mapping(source = "task.id", target = "taskId")
    CommentDto toDto(CommentEntity commentEntity);
    List<CommentDto> toDtoList(List<CommentEntity> commentEntities);

    Set<CommentDto> toDtoSet(Set<CommentEntity> commentEntitySet);
}