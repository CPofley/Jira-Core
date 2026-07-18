package com.api.jira.apis.task.repository;

import com.api.jira.apis.task.entity.TaskEntity;
import com.api.jira.apis.task.model.Priority;
import com.api.jira.apis.task.model.TaskStatus;
import com.api.jira.apis.task.model.TaskType;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TasksRepository extends JpaRepository<TaskEntity, Integer> {

    @Query("SELECT t FROM TaskEntity t WHERE t.id = :jiraId")
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
}
