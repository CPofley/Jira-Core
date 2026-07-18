package com.api.jira.apis.comment.controller;

import com.api.jira.apis.comment.models.CommentRequest;
import com.api.jira.apis.comment.models.UpdateCommentRequest;
import com.api.jira.apis.comment.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = "http://localhost:5173")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/save")
    public ResponseEntity<?> createComment(@Valid @RequestBody CommentRequest commentRequest) {
        return commentService.createComment(commentRequest);
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<?> getCommentsByTaskId(@PathVariable Integer taskId) {
        return commentService.getCommentsByTaskId(taskId);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateComment(@Valid @RequestBody UpdateCommentRequest updateCommentRequest){
        return commentService.updateComment(updateCommentRequest);
    }

}
