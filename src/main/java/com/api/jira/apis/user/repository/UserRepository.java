package com.api.jira.apis.user.repository;

import com.api.jira.apis.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    @Query("SELECT u FROM UserEntity u WHERE u.email = :email")
    Optional<UserEntity> findByEmail(String email);

    @Query("select u from UserEntity u where u.id = :projectId")
    Optional<UserEntity> findByUserId(@Param("projectId") Integer projectId);

}
