package com.api.jira.apis.task.service;


import com.api.jira.apis.project.entity.ProjectEntity;
import com.api.jira.apis.task.entity.TaskEntity;
import com.api.jira.apis.task.mapper.TaskMapper;
import com.api.jira.apis.task.model.Priority;
import com.api.jira.apis.task.model.TaskDto;
import com.api.jira.apis.task.model.TaskStatus;
import com.api.jira.apis.task.model.TaskType;
import com.api.jira.apis.task.repository.TasksRepository;
import com.api.jira.apis.user.entity.UserEntity;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TaskDbService {

    private final TasksRepository tasksRepository;
    private final TaskMapper taskMapper;

    public TaskDbService(TasksRepository tasksRepository, TaskMapper taskMapper) {
        this.tasksRepository = tasksRepository;
        this.taskMapper = taskMapper;
    }

    public TaskDto saveTask(TaskEntity taskEntity) {
        TaskEntity savedTask = tasksRepository.save(taskEntity);
        tasksRepository.flush();
        return taskMapper.toTaskDto(savedTask);
    }

    @CacheEvict(value = "tasks", key = "#parentId", condition = "#parentId != null")
    public TaskEntity flushChanges(TaskEntity taskEntity, Integer parentId){
        return tasksRepository.saveAndFlush(taskEntity);
    }

    public int updatePartialTask(Integer taskId, String title, String description, TaskType taskType, TaskStatus taskStatus, Priority priority,
                                 UserEntity updatedBy,UserEntity assignee, UserEntity reporter ,LocalDateTime updatedAt) {
        return tasksRepository.updatePartialTask(taskId, title, description, taskType, taskStatus, priority, updatedBy,
                updatedAt,assignee,reporter);
    }

    public TaskEntity getTaskByJiraId(Integer jiraId) {
//        // 1. Fetch task and its comments
//        TaskEntity task = tasksRepository.findByJiraIdWithComments(jiraId);
//
//        if (task != null) {
//            // 2. Fetch task and its sub-issues into the same persistence context
//            tasksRepository.findByJiraIdWithSubIssues(jiraId);
//        }
        /**
         * Added entityGraph in repo all and updated its fields to set in entity so no need of separate DB calls
         */
        return tasksRepository.findByJiraId(jiraId);
    }

    public List<TaskEntity> getTasksByStatus(TaskStatus taskStatus, Pageable pageable,Integer projectId) {
        return tasksRepository.findByProjectAndTaskStatus(taskStatus, pageable, projectId).getContent();
    }

    public List<TaskEntity> getTasksByPriority(Priority priority, Pageable pageable, Integer projectId) {

        return tasksRepository.findByProjectAndPriority(priority, pageable, projectId).getContent();
    }

    public List<TaskEntity> getTasksByTypes(TaskType taskType, Pageable pageable, Integer projectId) {
        return tasksRepository.findByProjectAndTaskType(taskType, pageable, projectId).getContent();
    }

    // 3. Evicts the task from cache immediately upon deletion
    // added condition to safely skip evaluation if the id value is null
    @CacheEvict(value = "tasks", key = "#a0", condition = "#a0 != null")
    public boolean deleteTask(Integer existingTaskId) {
        if (existingTaskId == null) {
            return false;
        }
        return tasksRepository.deleteByTaskId(existingTaskId) > 0;
    }

    public List<TaskEntity> getAllTasks(Pageable pageable) {
        return tasksRepository.findAll(pageable).getContent();
    }

    public Page<TaskEntity> getTasksForCurrentProject(Integer projectId, Pageable page){
        return tasksRepository.tasksByProjectId(projectId,page);
    }

    public Optional<ProjectEntity> getProjectByTask(Integer taskId){
        return tasksRepository.getProjectByTaskId(taskId);
    }

    public Page<TaskEntity> getSearchedTask(Integer projectId, String keyword,Pageable pageable){
        return tasksRepository.searchTasksGlobal( projectId,  keyword, pageable);
    }
}