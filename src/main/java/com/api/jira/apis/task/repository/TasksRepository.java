package com.api.jira.apis.task.repository;

import com.api.jira.apis.project.entity.ProjectEntity;
import com.api.jira.apis.task.entity.TaskEntity;
import com.api.jira.apis.task.model.Priority;
import com.api.jira.apis.task.model.TaskStatus;
import com.api.jira.apis.task.model.TaskType;
import com.api.jira.apis.user.entity.UserEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TasksRepository extends JpaRepository<TaskEntity, Integer> {

    // adding all lazy attributes since accessing any outside transaction was causing LazyInitializingException
    @EntityGraph(attributePaths = {
            "comments",
            "subIssues",
            "parentTask",
            "reporter",
            "assignee",
            "project",
            "updatedBy"
    })
    @Query("SELECT distinct t FROM TaskEntity t WHERE t.id = :jiraId")
    TaskEntity findByJiraId(@Param("jiraId") Integer jiraId);

    // Finds a chunk of tasks filtered by status, ordered by creation date
    @Query("SELECT t FROM TaskEntity t WHERE t.taskStatus = :taskStatus AND t.project.id = :projectId ORDER BY t.createdAt DESC")
    Slice<TaskEntity> findByProjectAndTaskStatus(@Param("taskStatus") TaskStatus taskStatus, Pageable pageable, @Param("projectId") Integer projectId);

    @Query("select t from TaskEntity t where t.priority = :priority AND t.project.id = :projectId order by t.createdAt desc")
    Slice<TaskEntity> findByProjectAndPriority(Priority priority, Pageable pageable, @Param("projectId") Integer projectId);

    @Query("select t from TaskEntity t where t.taskType = :taskType AND t.project.id = :projectId order by t.createdAt desc")
    Slice<TaskEntity> findByProjectAndTaskType(TaskType taskType, Pageable pageable, @Param("projectId") Integer projectId);

    @Transactional
    @Modifying
    @Query("DELETE FROM TaskEntity t WHERE t.id = :taskId")
    int deleteByTaskId(Integer taskId);

    @Query("select t from TaskEntity t where t.project.projectId= :projectId")
    Page<TaskEntity> tasksByProjectId(@Param("projectId") Integer projectId, Pageable pageable);


    // cannot fetch two left join list , so we need to fetch them separately and then merge them in the service layer
    @Query("SELECT t FROM TaskEntity t " +
            "LEFT JOIN FETCH t.comments " +
            "LEFT JOIN FETCH t.subIssues " +
            "WHERE t.id = :jiraId")
    TaskEntity findByJiraIdWithDetails(@Param("jiraId") Integer jiraId);


    // Query 1: Fetch the task along with comments
    @Query("SELECT t FROM TaskEntity t LEFT JOIN FETCH t.comments WHERE t.id = :jiraId")
    TaskEntity findByJiraIdWithComments(@Param("jiraId") Integer jiraId);

    // Query 2: Fetch the task along with sub-issues
    @Query("SELECT t FROM TaskEntity t LEFT JOIN FETCH t.subIssues WHERE t.id = :jiraId")
    TaskEntity findByJiraIdWithSubIssues(@Param("jiraId") Integer jiraId);

    @Query("SELECT t.project FROM TaskEntity t where t.id= :jiraId")
    Optional<ProjectEntity> getProjectByTaskId(@Param("jiraId") Integer jiraId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE TaskEntity t SET " +
            "t.title = COALESCE(:title, t.title), " +
            "t.description = COALESCE(:description, t.description), " +
            "t.taskType = COALESCE(:taskType, t.taskType), " +
            "t.taskStatus = COALESCE(:taskStatus, t.taskStatus), " +
            "t.priority = COALESCE(:priority, t.priority), " +
            "t.updatedBy = :updatedBy, " +
            "t.updatedAt = :updatedAt " +
            "WHERE t.id = :jiraId")
    int updatePartialTask(@Param("jiraId") Integer jiraId,
                          @Param("title") String title,
                          @Param("description") String description,
                          @Param("taskType") TaskType taskType,
                          @Param("taskStatus") TaskStatus taskStatus,
                          @Param("priority") Priority priority,
                          @Param("updatedBy") UserEntity updatedBy,
                          @Param("updatedAt") LocalDateTime updatedAt);
}
