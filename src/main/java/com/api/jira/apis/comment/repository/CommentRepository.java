package com.api.jira.apis.comment.repository;

import com.api.jira.apis.comment.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Integer> {

    @Query("SELECT c FROM CommentEntity c WHERE c.task.id = :taskId ORDER BY c.id DESC")
    List<CommentEntity> findByTaskId(@Param("taskId") Integer taskId);

    @Query("SELECT c FROM CommentEntity c WHERE c.id = :commentId")
    Optional<CommentEntity> findByCommentId(@Param("commentId") Integer commentId);
}
