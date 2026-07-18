package com.api.jira.apis.task.service;


import com.api.jira.apis.task.entity.TaskEntity;
import com.api.jira.apis.task.mapper.TaskMapper;
import com.api.jira.apis.task.model.Priority;
import com.api.jira.apis.task.model.TaskDto;
import com.api.jira.apis.task.model.TaskStatus;
import com.api.jira.apis.task.model.TaskType;
import com.api.jira.apis.task.repository.TasksRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskDbService {

    private final TasksRepository tasksRepository;
    private final TaskMapper taskMapper;

    public TaskDbService(TasksRepository tasksRepository, TaskMapper taskMapper) {
        this.tasksRepository = tasksRepository;
        this.taskMapper = taskMapper;
    }

    // 2. Pre-caches a newly created task so the details page redirection hits the cache instantly
    @CachePut(value = "tasks", key = "#taskEntity.id", condition = "#taskEntity.id != null")
    public Integer saveTask(TaskEntity taskEntity) {
        TaskEntity savedTask = tasksRepository.save(taskEntity);
        return savedTask.getId();
    }

    public TaskEntity getTaskByJiraId(Integer jiraId) {
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

}
