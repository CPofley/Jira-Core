package com.api.jira.apis.task.entity;

import com.api.jira.apis.comment.entity.CommentEntity;
import com.api.jira.apis.project.entity.ProjectEntity;
import com.api.jira.apis.task.model.Priority;
import com.api.jira.apis.task.model.TaskStatus;
import com.api.jira.apis.task.model.TaskType;
import com.api.jira.apis.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "tasks")
@Data
@ToString(exclude = "comments")
@EqualsAndHashCode(exclude = "comments")
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @Column(name = "type", nullable = false)
    private TaskType taskType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private UserEntity assignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private UserEntity reporter;

    @Column(name = "status")
    private TaskStatus taskStatus;

    @Column(name = "priority")
    private Priority priority;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

   @Column(name = "created_by",nullable = false)
    private String createdBy;
// orphanRemoval means is we delete a task , its associated comments will be orphans so setting it to true will remove those orphans too
    // cascadeType means drill down i.e all underlying associated things
    // so any change will propagate to the underlying associated things update / delete etc.
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentEntity> comments;

    // 1. THE PARENT LINK (Many issues can point to the same parent)
    // created this to determine parent tasks for current task
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="parent_id") // Creates the 'parent_id' column in DB table
    private TaskEntity parentTask;

    // 2. THE CHILDREN LINK (One parent issue can have a list of child issues underneath it)
    // Created this to determine child tasks for current task
    @OneToMany(mappedBy = "parentTask", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskEntity> subIssues;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false) // Links this task to a specific project
    private ProjectEntity project;

    // Helper methods to keep both sides of the relationship in sync
    public void addComment(CommentEntity comment) {
        comments.add(comment);
        comment.setTask(this);
    }

    public void removeComment(CommentEntity comment) {
        comments.remove(comment);
        comment.setTask(null);
    }

    public void addChildTask(TaskEntity child) {
        if (this.subIssues == null) {
            this.subIssues = new java.util.ArrayList<>();
        }
        this.subIssues.add(child);
        child.setParentTask(this);
    }
}
