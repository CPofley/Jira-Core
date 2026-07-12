package com.api.jira.apis.task.controller;

import com.api.jira.apis.task.model.*;
import com.api.jira.apis.task.service.TasksService;
import jakarta.validation.Valid;
import lombok.NonNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// security pwd: b4ef1b4f-810b-4999-9617-1c88be3953eb
@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "http://localhost:5173") // Enables CORS specifically for your local Vite React dev app
public class TasksController {
    private final TasksService tasksService;

    public TasksController(TasksService tasksService) {
        this.tasksService = tasksService;
    }

    @PostMapping("/create")
    ResponseEntity<?> createTask(@Valid @NonNull @RequestBody CreateTaskRequest createTaskRequest,
                                 JwtAuthenticationToken auth) {
        Jwt jwt = auth.getToken();
        String reporterEmail = jwt.getClaimAsString("email");
        String reporterName = jwt.getClaimAsString("name");

        createTaskRequest.setReporter(reporterEmail);
        createTaskRequest.setCreatedBy(reporterName);
        Integer taskId = tasksService.createTask(createTaskRequest);
        return ResponseEntity.ok(Map.of("taskId", taskId));
    }

    @GetMapping("/get/created-task")
    ResponseEntity<?> getCreatedTask(@RequestParam Integer taskId) {
        // Here you can implement the logic to get tasks created by a specific user
        ResponseEntity<GetTaskDetailsResponse> response =  tasksService.getTaskDetails(taskId);
        if(response.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.ok(response.getBody());
        } else {
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        }
    }
    @GetMapping("/config")
    public ResponseEntity<TaskMetadataConfig> getTaskMetadataConfig() {
        TaskMetadataConfig config = TaskMetadataConfig.builder()
                .statuses(List.of(TaskStatus.values()))     // 🛠️ Pass as raw Enum List
                .priorities(List.of(Priority.values()))   // 🛠️ Pass as raw Enum List
                .taskTypes(List.of(TaskType.values()))     // 🛠️ Pass as raw Enum List
                .build();

        return ResponseEntity.ok(config);
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<?> updateTaskPartially(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> updates) {

        // 1. Pass the task ID and the partial fields map to your service layer
        ResponseEntity<TaskDto> response = tasksService.updateTaskFields(id, updates);


        if (response.getStatusCode().is2xxSuccessful()) {
            // Return the updated task body object directly so UI gets the fresh state payload
            return ResponseEntity.ok(response.getBody());
        } else {
            return ResponseEntity.status(response.getStatusCode()).body(Map.of("error", "Failed to update task"));
        }
    }


    @GetMapping("/by-status/{projectId}")
    public ResponseEntity<List<TaskDto>> getTasksByStatusPaged(
            @RequestParam TaskStatus status,
            @PathVariable Integer projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        List<TaskDto> tasks = tasksService.getTasksByProjectAndStatus(status, pageable,projectId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/by-priority/{projectId}")
    public ResponseEntity<List<TaskDto>> getTasksByStatusPaged(
            @RequestParam Priority priority,
            @PathVariable Integer projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        List<TaskDto> tasks = tasksService.getTasksByProjectAndPriority(priority, pageable, projectId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/by-task-type/{projectId}")
    public ResponseEntity<List<TaskDto>> getTasksByStatusPaged(
            @RequestParam TaskType type,
            @PathVariable Integer projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        List<TaskDto> tasks = tasksService.getTasksByProjectAndTypes(type, pageable,projectId);
        return ResponseEntity.ok(tasks);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Integer id) {
        boolean deleted = tasksService.deleteTask(id);
        try {
            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "Task deleted successfully"));
            } else {
                return ResponseEntity.status(404).body(Map.of("error", "Task not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "An error occurred while deleting the task"));
        }
    }

    @PostMapping("/link-tasks")
    public ResponseEntity<?> linkTasks(@RequestBody LinkTaskRequest linkTaskRequest) {
        return tasksService.linkTasks(linkTaskRequest);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TaskDto>> getTasksFromProject(
            @PathVariable Integer projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        List<TaskDto> tasks = tasksService.taskLinkedToProject(projectId, pageable);

        if (tasks.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(tasks);
    }


}
