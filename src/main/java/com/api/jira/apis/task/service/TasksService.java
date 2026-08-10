package com.api.jira.apis.task.service;

import com.api.jira.apis.comment.mapper.CommentMapper;
import com.api.jira.apis.exceptions.ExceptionTypes.EmailFormatException;
import com.api.jira.apis.exceptions.ExceptionTypes.TaskNotFoundException;
import com.api.jira.apis.exceptions.ExceptionTypes.UserNotFoundException;
import com.api.jira.apis.project.entity.ProjectEntity;
import com.api.jira.apis.project.service.ProjectDbService;
import com.api.jira.apis.task.entity.TaskEntity;
import com.api.jira.apis.task.mapper.TaskMapper;
import com.api.jira.apis.task.model.*;
import com.api.jira.apis.user.entity.UserEntity;
import com.api.jira.apis.user.mapper.UserMapper;
import com.api.jira.apis.user.service.UserDbService;
import java.util.regex.Pattern;
import org.springframework.cache.Cache;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executor;

@Transactional
@Service
public class TasksService {

    private final TaskMapper taskMapper;
    private final TaskDbService taskDbService;
    private final CommentMapper commentMapper;
    private final UserDbService userDbService;
    private final UserMapper userMapper;
    private final ProjectDbService projectDbService;
    private final CacheManager cacheManager;
    private final Executor threadConfig;
    private final SimpMessagingTemplate messagingTemplate;
    private final ApplicationEventPublisher eventPublisher;

    public TasksService(TaskMapper taskMapper, TaskDbService taskDbService, CommentMapper commentMapper, UserDbService userDbService, UserMapper userMapper, ProjectDbService projectDbService, CacheManager cacheManager,
                        @Qualifier("customExecutor") Executor threadConfig, SimpMessagingTemplate messagingTemplate, ApplicationEventPublisher eventPublisher) {
        this.taskMapper = taskMapper;
        this.taskDbService = taskDbService;
        this.commentMapper = commentMapper;
        this.userDbService = userDbService;
        this.userMapper = userMapper;
        this.projectDbService = projectDbService;
        this.cacheManager = cacheManager;
        this.threadConfig = threadConfig;
        this.messagingTemplate = messagingTemplate;
        this.eventPublisher = eventPublisher;
    }

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"
    );

    public Integer createTask(CreateTaskRequest createTaskRequest) {
        TaskEntity taskEntity = taskMapper.toTaskEntity(createTaskRequest);
        UserEntity reporterEntity = userDbService.findByEmail(createTaskRequest.getReporter())
                .orElseThrow(() -> new RuntimeException("Reporter not found with email: " + taskEntity.getReporter()));
        Optional<ProjectEntity> projectEntity = projectDbService.findProjectById(createTaskRequest.getProjectId());
        if(projectEntity.isEmpty()){
            throw new TaskNotFoundException("There is not active project "+createTaskRequest.getProjectId()+" to map this task.");
        }
        taskEntity.setProject(projectEntity.get());
        taskEntity.setReporter(reporterEntity);
        if(createTaskRequest.getAssignee() != null && !createTaskRequest.getAssignee().isEmpty()) {
            userDbService.findByEmail(createTaskRequest.getAssignee())
                    .ifPresent(taskEntity::setAssignee);
        }
        if (taskEntity.getDescription() == null) {
            taskEntity.setDescription("");
        }
        if(validateRequest(taskEntity)){
            TaskDto dto =  taskDbService.saveTask(taskEntity);
            if(dto != null && dto.getId() != null){
                return dto.getId();
            }
            else{
                throw new RuntimeException("Task creation failed.");
            }
        }
        else {
            throw new RuntimeException("Assignee and Reporter are mandatory fields.");
        }

    }

    private boolean validateRequest(TaskEntity taskEntity) {
        if(Objects.isNull(taskEntity.getAssignee()) || Objects.isNull(taskEntity.getReporter())){
            throw new RuntimeException("Assignee and Reporter are mandatory fields.");
        }
        else
            return true;
    }

    @Cacheable(value = "tasks", key = "#jiraId", condition = "#jiraId != null", unless = "#result == null")
    public TaskDto getTaskDetails(Integer jiraId) {
        TaskEntity taskEntity = taskDbService.getTaskByJiraId(jiraId);

        if (taskEntity == null) {
            throw new TaskNotFoundException("Task not found with task ID: " + jiraId);
        }
        return taskMapper.toTaskDto(taskEntity);
    }

    @Transactional
    public TaskDto updateTaskFields(UpdateTaskRequest updateTaskRequest) {
        Map<String, Object> updates = updateTaskRequest.getFields();
        Integer id = updateTaskRequest.getTaskId();
        // 1. Fetch managed entity once
        TaskEntity task = taskDbService.getTaskByJiraId(id);
        if(task == null) {
            throw new TaskNotFoundException("Task not found with task ID: " + id);
        }
        if(!isValidEmail(updateTaskRequest.getEmailId())){
            throw new EmailFormatException("Incorrect email format: "+updateTaskRequest.getEmailId());
        }
        UserEntity updatedBy = userDbService.findByEmail(updateTaskRequest.getEmailId())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + updateTaskRequest.getEmailId()));

        // 2. Conditionally update fields if key exists in payload
        if (updates.containsKey("title")) {
            task.setTitle((String) updates.get("title"));
        }
        if (updates.containsKey("description")) {
            task.setDescription((String) updates.get("description"));
        }
        if (updates.containsKey("taskType") && updates.get("taskType") != null) {
            task.setTaskType(TaskType.valueOf((String) updates.get("taskType")));
        }
        if (updates.containsKey("taskStatus") && updates.get("taskStatus") != null) {
            task.setTaskStatus(TaskStatus.valueOf((String) updates.get("taskStatus")));
        }
        if (updates.containsKey("priority") && updates.get("priority") != null) {
            task.setPriority(Priority.valueOf((String) updates.get("priority")));
        }
        if (updates.containsKey("assignee")) {
            String email = (String) updates.get("assignee");
            task.setAssignee(email != null ? userDbService.findByEmail(email).orElse(null) : null);
        }
        if (updates.containsKey("reporter")) {

            String email = (String) updates.get("reporter");
           if(!email.isEmpty() && !email.isBlank()&& !isValidEmail(email)){
                throw new EmailFormatException("Incorrect reporter email format: "+email);
           }
            task.setReporter(userDbService.findByEmail(email).orElse(null));
           if(task.getReporter()==null){
               throw new UserNotFoundException("user not found with email: "+email);
           }
        }

        task.setUpdatedBy(updatedBy);
        task.setUpdatedAt(LocalDateTime.now());
        TaskDto updatedtaskDto = taskDbService.saveTask(task);
        Cache tasksCache = cacheManager.getCache("tasks");
        if (tasksCache != null) {
            tasksCache.evict(id);
        }
        TaskUpdateEventDto eventDto = TaskUpdateEventDto.builder().
                taskId(id)
                .updatedBy(updateTaskRequest.getEmailId())
                .fields(updates)
                .type("FIELD_UPDATE")
                .build();
        eventPublisher.publishEvent(eventDto);
        return updatedtaskDto;
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

    @CacheEvict(value = "tasks", key = "#id")
    public boolean deleteTask(Integer id) {
        TaskEntity existingTask = taskDbService.getTaskByJiraId(id);
        Integer parentJiraId = null;
        if(existingTask.getParentTask()!= null){
            parentJiraId = existingTask.getParentTask().getId();
        }
        boolean result = taskDbService.deleteTask(existingTask.getId());
        if (parentJiraId != null && cacheManager.getCache("tasks") != null) {
            cacheManager.getCache("tasks").evict(parentJiraId);
        }
        return result;
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
        // Clean up cache states programmatically for both IDs
        // 🟢 Fixed: Evicting directly from "tasks" cache region
        if (cacheManager.getCache("tasks") != null) {
            cacheManager.getCache("tasks").evict(linkTaskRequest.getCurrentTaskId());
            cacheManager.getCache("tasks").evict(linkTaskRequest.getTaskToLinkId());
        }

        return ResponseEntity.ok("Issues linked successfully.");
    }

    public List<TaskDto> taskLinkedToProject(Integer projectId, Pageable pageable){
        return taskMapper.toTaskDtoList(taskDbService.getTasksForCurrentProject(projectId,pageable).getContent().stream().sorted(Comparator.comparing(TaskEntity::getCreatedAt).reversed()).toList());
    }

    @CacheEvict(value = "tasks", key = "#createSubTaskRequest.currentTaskId")
    public TaskDto createSubTask(CreateSubTaskRequest createSubTaskRequest) {
        TaskEntity parentTask = taskDbService.getTaskByJiraId(createSubTaskRequest.getCurrentTaskId());

        TaskEntity newSubTask = new TaskEntity();
        newSubTask.setTitle(createSubTaskRequest.getTitle());
        newSubTask.setTaskType(createSubTaskRequest.getTaskType() != null ? createSubTaskRequest.getTaskType() : TaskType.SUB_TASK);
        newSubTask.setTaskStatus(TaskStatus.TO_DO);
        newSubTask.setPriority(Priority.LOW);
        newSubTask.setCreatedAt(LocalDateTime.now());
        newSubTask.setDescription("");
        newSubTask.setProject(parentTask.getProject());

        // Set foreign key directly
        newSubTask.setParentTask(parentTask);

        TaskEntity savedSubTask = taskDbService.flushChanges(newSubTask, parentTask.getId());
        return taskMapper.toTaskDto(savedSubTask);
    }

    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
}