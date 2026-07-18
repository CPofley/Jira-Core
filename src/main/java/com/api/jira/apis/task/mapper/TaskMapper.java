package com.api.jira.apis.task.mapper;

import com.api.jira.apis.comment.mapper.CommentMapper;
import com.api.jira.apis.task.entity.TaskEntity;
import com.api.jira.apis.task.model.*;
import com.api.jira.apis.user.entity.UserEntity;
import com.api.jira.sharedMapper.mapper.CommonMapper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {CommonMapper.class, CommentMapper.class})
public interface TaskMapper {

    // 1. TELL MAPSTRUCT TO IGNORE THESE FIELDS DURING CREATION
    @Mapping(target = "assignee", ignore = true)
    @Mapping(target = "reporter", ignore = true)
    TaskEntity toTaskEntity(CreateTaskRequest createTaskRequest);

    // Main mapping method
    TaskDto toTaskDto(TaskEntity taskEntity);

    List<TaskDto> toTaskDtoList(List<TaskEntity> taskEntities);

    // ─── EXPLICIT RELATIONSHIP MAPPERS TO BREAK CYCLES ───

    // Explicitly handles mapping the parent entity to ParentTaskDto safely
    ParentTaskDto toParentTaskDto(TaskEntity taskEntity);

    // Explicitly handles mapping child subIssues elements to ChildTasksDto safely
    ChildTasksDto toChildTasksDto(TaskEntity taskEntity);

    List<ChildTasksDto> toChildTasksDtoList(List<TaskEntity> taskEntities);

    default Date dateFormatter(String transactionDateStr) throws ParseException {
        Date transactionDate;
        String pattern = transactionDateStr.contains("/") ? "d/M/yyyy" : "dd-MM-yyyy";
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(pattern);
            formatter.setLenient(false);
            transactionDate = formatter.parse(transactionDateStr);
        } catch (ParseException e) {
            System.err.println("Error parsing date: " + e.getMessage());
            throw e;
        }
        return transactionDate;
    }

    @AfterMapping
    default void handleCustomMapping(CreateTaskRequest request, @MappingTarget TaskEntity entity) {
        if (entity.getTaskStatus() == null) {
            entity.setTaskStatus(TaskStatus.TO_DO);
        }
        if(entity.getPriority() == null){
            entity.setPriority(Priority.LOW);
        }
        entity.setCreatedAt(LocalDateTime.now());
    }

    // 2. THIS METHOD HANDLES THE REVERSE: Entity -> String (for returning data to React)
    default String mapUserEntityToString(UserEntity user) {
        if (user == null) {
            return null;
        }
        // Returns the user's name to the frontend UI
        return user.getUsername() != null ? user.getUsername() : user.getEmail();
    }
}