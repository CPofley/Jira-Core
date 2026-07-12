package com.api.jira.apis.task.service;

import com.api.jira.apis.comment.mapper.CommentMapper;
import com.api.jira.apis.project.entity.ProjectEntity;
import com.api.jira.apis.project.service.ProjectDbService;
import com.api.jira.apis.task.entity.TaskEntity;
import com.api.jira.apis.task.mapper.TaskMapper;
import com.api.jira.apis.task.model.*;
import com.api.jira.apis.user.entity.UserEntity;
import com.api.jira.apis.user.mapper.UserMapper;
import com.api.jira.apis.user.service.UserDbService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TasksService {

    private final TaskMapper taskMapper;
    private final TaskDbService taskDbService;
    private final CommentMapper commentMapper;
    private final UserDbService userDbService;
    private final UserMapper userMapper;
    private final ProjectDbService projectDbService;

    public TasksService(TaskMapper taskMapper, TaskDbService taskDbService, CommentMapper commentMapper, UserDbService userDbService, UserMapper userMapper, ProjectDbService projectDbService) {
        this.taskMapper = taskMapper;
        this.taskDbService = taskDbService;
        this.commentMapper = commentMapper;
        this.userDbService = userDbService;
        this.userMapper = userMapper;
        this.projectDbService = projectDbService;
    }

    public Integer createTask(CreateTaskRequest createTaskRequest) {
        TaskEntity taskEntity = taskMapper.toTaskEntity(createTaskRequest);
        UserEntity reporterEntity = userDbService.findByEmail(createTaskRequest.getReporter())
                .orElseThrow(() -> new RuntimeException("Reporter not found with email: " + taskEntity.getReporter()));
        Optional<ProjectEntity> projectEntity = projectDbService.findProjectById(createTaskRequest.getProjectId());
        if(projectEntity.isEmpty()){
            throw new RuntimeException("There is not active project "+createTaskRequest.getProjectId()+" to map this task.");
        }
        taskEntity.setProject(projectEntity.get());
        taskEntity.setReporter(reporterEntity);
        taskEntity.setCreatedBy(createTaskRequest.getCreatedBy());
        if(createTaskRequest.getAssignee() != null && !createTaskRequest.getAssignee().isEmpty()) {
            userDbService.findByEmail(createTaskRequest.getAssignee())
                    .ifPresent(taskEntity::setAssignee);
        }
        return taskDbService.saveTask(taskEntity);
    }

    public ResponseEntity<GetTaskDetailsResponse> getTaskDetails(Integer jiraId) {
        TaskEntity taskEntity = taskDbService.getTaskByJiraId(jiraId);

        if (taskEntity == null) {
            throw new RuntimeException("Task not found with jiraId: " + jiraId);
        }
        GetTaskDetailsResponse response = new GetTaskDetailsResponse(taskMapper.toTaskDto(taskEntity));
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<TaskDto> updateTaskFields(Integer id, Map<String, Object> updates) {
        TaskEntity existingTask = taskDbService.getTaskByJiraId(id);
        if (existingTask == null) {
            throw new RuntimeException("Task not found with id: " + id);
        }

        updates.forEach((key, value) -> {
            switch (key) {
                case "title" -> existingTask.setTitle((String) value);
                case "description" -> existingTask.setDescription((String) value);
                case "assignee" -> existingTask.setAssignee((UserEntity) value);
                case "reporter" -> existingTask.setReporter( (UserEntity)value);
                case "taskStatus" -> existingTask.setTaskStatus(Enum.valueOf(TaskStatus.class, (String) value));
                case "priority" -> existingTask.setPriority(
                        value != null ? Priority.valueOf(value.toString().toUpperCase()) : null
                );
                case "taskType" -> existingTask.setTaskType(Enum.valueOf(TaskType.class, (String) value));
                default -> throw new IllegalArgumentException("Invalid field: " + key);
            }
        });

        taskDbService.saveTask(existingTask);
        return ResponseEntity.ok(taskMapper.toTaskDto(existingTask));
    }

    public List<TaskDto> getTasksByProjectAndStatus(TaskStatus status, Pageable pageable, Integer projectId) {
        List<TaskEntity> taskEntities = taskDbService.getTasksByStatus(status, pageable,projectId);
        return taskMapper.toTaskDtoList(taskEntities);
    }

    public List<TaskDto> getTasksByProjectAndPriority(Priority priority, Pageable pageable, Integer projectId) {
        List<TaskEntity> taskEntities = taskDbService.getTasksByPriority(priority, pageable, projectId);
        return taskMapper.toTaskDtoList(taskEntities);
    }

    public List<TaskDto> getTasksByProjectAndTypes(TaskType taskType, Pageable pageable, Integer projectId) {
        List<TaskEntity> taskEntities = taskDbService.getTasksByTypes(taskType, pageable, projectId);
        return taskMapper.toTaskDtoList(taskEntities);
    }

    public boolean deleteTask(Integer id) {
        TaskEntity existingTask = taskDbService.getTaskByJiraId(id);
        if (existingTask == null) {
            return false;
        } else {
            return taskDbService.deleteTask(existingTask.getId());
        }
    }

    public List<TaskDto> getAllTasks(Pageable pageable) {
        List<TaskEntity> taskEntities = taskDbService.getAllTasks(pageable);
        return taskMapper.toTaskDtoList(taskEntities);
    }

    @Transactional
    public ResponseEntity<?> linkTasks(LinkTaskRequest linkTaskRequest) {
        TaskEntity currentTask = taskDbService.getTaskByJiraId(linkTaskRequest.getCurrentTaskId());
        TaskEntity taskToLink = taskDbService.getTaskByJiraId(linkTaskRequest.getTaskToLinkId());

        if (currentTask == null || taskToLink == null) {
            throw new RuntimeException("One or both tasks not found");
        }
        if (!currentTask.getProject().getProjectId().equals(taskToLink.getProject().getProjectId())) {
            throw new RuntimeException("Validation Failed: Both tasks must belong to the exact same workspace to be linked.");
        }

        switch (currentTask.getTaskType()) {
            case EPIC -> {
                if (taskToLink.getTaskType().equals(TaskType.STORY)) {
                    if (taskToLink.getParentTask() != null) {
                        throw new RuntimeException("This Story already belongs to another Epic: "
                                + taskToLink.getParentTask().getTitle());
                    }
                    currentTask.addChildTask(taskToLink);
                } else {
                    throw new RuntimeException("Only Stories can be linked directly under an Epic");
                }
            }

            case STORY -> {
                if (taskToLink.getTaskType().equals(TaskType.EPIC)) {
                    if (currentTask.getParentTask() != null) {
                        throw new RuntimeException("This Story already belongs to Epic: "
                                + currentTask.getParentTask().getTitle());
                    }
                    currentTask.setParentTask(taskToLink);
                } else if (taskToLink.getTaskType() == TaskType.BUG ||
                        taskToLink.getTaskType() == TaskType.TASK ||
                        taskToLink.getTaskType() == TaskType.SUB_TASK) {
                    if (taskToLink.getParentTask() != null) {
                        throw new RuntimeException("This " + taskToLink.getTaskType()
                                + " already belongs to Story: " + taskToLink.getParentTask().getTitle());
                    }
                    currentTask.addChildTask(taskToLink); // Consistent helper assignment
                } else {
                    throw new RuntimeException("A Story cannot be linked to another Story");
                }
            }

            case BUG, TASK, SUB_TASK -> {
                if (taskToLink.getTaskType().equals(TaskType.STORY)) {
                    if (currentTask.getParentTask() != null) {
                        throw new RuntimeException("This issue already has a parent Story assigned: "
                                + currentTask.getParentTask().getTitle());
                    }
                    currentTask.setParentTask(taskToLink);
                } else {
                    throw new RuntimeException("Bugs, Tasks, and Sub-tasks can only be linked to a parent Story");
                }
            }

            default -> throw new RuntimeException("Invalid task type for linking");
        }

        taskDbService.saveTask(currentTask);
        taskDbService.saveTask(taskToLink);

        return ResponseEntity.ok("Issues linked successfully.");
    }

    public List<TaskDto> taskLinkedToProject(Integer projectId, Pageable pageable){
        return taskMapper.toTaskDtoList(taskDbService.getTasksForCurrentProject(projectId,pageable).getContent());
    }
}