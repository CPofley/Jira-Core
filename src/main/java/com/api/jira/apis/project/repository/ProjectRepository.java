package com.api.jira.apis.project.repository;

import com.api.jira.apis.project.entity.ProjectEntity;
import com.api.jira.apis.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<ProjectEntity,Integer> {

    @Query("select p from ProjectEntity p where p.id= :projectId")
    Optional<ProjectEntity> getProjectByProjectId(@Param("projectId") Integer projectId);

    @Query("select p.users from ProjectEntity p where p.id = :projectId")
    List<UserEntity> getUsersTaggedToProject(@Param("projectId") Integer projectId);

    List<ProjectEntity> findByUsers_Email(String email);
}
