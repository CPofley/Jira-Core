package com.api.jira.apis.comment.entity;

import com.api.jira.apis.task.entity.TaskEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;       // 🛠️ Import ToString
import lombok.EqualsAndHashCode; // 🛠️ Import EqualsAndHashCode

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
@ToString(exclude = "task") // 🛠️ FIX: Prevents infinite loop during logging/debugging
@EqualsAndHashCode(exclude = "task") // 🛠️ FIX: Prevents infinite loops in collections
public class CommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    private TaskEntity task;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;

    private String author;

    private LocalDateTime timestamp;

    @Column(name = "updated", nullable = false)
    private boolean updated = false;

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }
}