package com.api.jira.apis.comment.service;

import com.api.jira.apis.comment.entity.CommentEntity;
import com.api.jira.apis.comment.models.CommentResponse;
import com.api.jira.apis.comment.repository.CommentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentDbService {
    private final CommentRepository commentRepository;


    public CommentDbService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public CommentResponse saveComment(CommentEntity commentEntity) {
        CommentEntity savedComments = commentRepository.save(commentEntity);

       return CommentResponse.builder()
                .id(savedComments.getId())
                .comment(savedComments.getComment())
                .author(savedComments.getAuthor())
                .taskId(savedComments.getTask().getId())
               .updated(savedComments.isUpdated())
                .build();
    }

    public List<CommentEntity> getCommentsByTaskId(Integer taskId) {
        return commentRepository.findByTaskId(taskId);
    }

    public CommentEntity getCommentById(Integer commentId) {
        return commentRepository.findByCommentId(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));
    }
}
