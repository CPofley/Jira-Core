package com.api.jira.apis.user.entity;

import com.api.jira.apis.project.entity.ProjectEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Column(name = "username", nullable = false, unique = true)
    private String username;
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    private String pictureUrl; // Google provides their profile picture!
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Tell Hibernate that ProjectEntity manages this relationship
    @ManyToMany(mappedBy = "users", fetch = FetchType.LAZY)
    private List<ProjectEntity> projects;
}
