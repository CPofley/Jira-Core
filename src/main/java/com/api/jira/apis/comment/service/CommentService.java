package com.api.jira.apis.comment.service;

import com.api.jira.apis.comment.entity.CommentEntity;
import com.api.jira.apis.comment.models.CommentRequest;
import com.api.jira.apis.comment.models.CommentResponse;
import com.api.jira.apis.comment.models.UpdateCommentRequest;
import com.api.jira.apis.task.entity.TaskEntity;
import com.api.jira.apis.task.service.TaskDbService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentDbService commentDbService;
    private final TaskDbService taskDbService;

    public CommentService(CommentDbService commentDbService, TaskDbService taskDbService) {
        this.commentDbService = commentDbService;
        this.taskDbService = taskDbService;
    }

    @Transactional
    // 🛠️ Evicts the specific task from cache so the next Detail Page load gets the fresh comment stream from DB
    @CacheEvict(value = "taskCache", key = "#commentRequest.taskId", beforeInvocation = true)
    public ResponseEntity<?> createComment(CommentRequest commentRequest) {
        TaskEntity existingTask = taskDbService.getTaskByJiraId(commentRequest.getTaskId());
        CommentEntity commentEntity = new CommentEntity();
        commentEntity.setComment(commentRequest.getComment());
        commentEntity.setAuthor(commentRequest.getAuthor());
        existingTask.addComment(commentEntity);
        CommentResponse response = commentDbService.saveComment(commentEntity);
        if(!Objects.isNull(response)){
            return ResponseEntity.ok(response);
        }
        else
            return ResponseEntity.badRequest().body("Error on saving comments. Please try again later.");
    }

    public ResponseEntity<List<CommentResponse>> getCommentsByTaskId(Integer taskId) {
        List<CommentEntity> entities = commentDbService.getCommentsByTaskId(taskId);

        List<CommentResponse> responses = entities.stream().map(c ->
                CommentResponse.builder()
                        .id(c.getId())
                        .comment(c.getComment())
                        .author(c.getAuthor())
                        .taskId(c.getTask().getId())
                        // If you have a createdAt field, map it here too!
                        .build()
        ).collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    public ResponseEntity<?> updateComment(UpdateCommentRequest updateCommentRequest) {
        CommentEntity existingComment = commentDbService.getCommentById(updateCommentRequest.getCommentId());
        if (existingComment == null) {
            return ResponseEntity.notFound().build();
        }

        existingComment.setComment(updateCommentRequest.getComment());
        existingComment.setUpdated(true);

        CommentResponse updatedResponse = commentDbService.saveComment(existingComment);
        return ResponseEntity.ok(updatedResponse);
    }
}
