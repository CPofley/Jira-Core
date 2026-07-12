package com.api.jira.apis.project.entity;

import com.api.jira.apis.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * @Note:
 * (Note: Whenever you create bidirectional relationships in Lombok, you MUST add @ToString(exclude = "...")
 * and @EqualsAndHashCode(exclude = "..."). If you don't, when you print a User, it prints the Project,
 * which prints the User, which prints the Project... resulting in an infinite loop and a massive StackOverflowError crash!)
 */

@Entity
@Data
@Table(name = "projects")
@ToString(exclude = "users")
@EqualsAndHashCode(exclude = "users")
public class ProjectEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer projectId;

    // many projects can have many users, and many users can belong to many projects
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "project_users",
            // The column in project_users linking to ProjectEntity's 'id'
            joinColumns = @JoinColumn(name = "project_id"),
            // The column in project_users linking to UserEntity's 'id'
            inverseJoinColumns = @JoinColumn(name = "user_id")
            /**
             * This will create a join table named 'project_users' with two columns: 'project_id' and 'user_id'.
             * Each row in this table represents a relationship between a project and a user.
             * For example, if project with ID 1 has users with IDs 2 and 3, the 'project_users' table will have two rows:
             * | project_id | | user_id |
             * |------------|---------|
             * | 1          | 2       |
             * | 1          | 3       |
             * This allows for a many-to-many relationship between projects and users.
            */
    )
    private List<UserEntity> users = new ArrayList<>();

    @Column(name = "project_name")
    private String projectName;
    @Column(name = "projectDescription")
    private String projectDescription;

}
