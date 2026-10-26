package com.yas.max.rest.repository;

import com.yas.max.dto.enums.TaskStatus;
import com.yas.max.rest.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, String> {
    long countByUserIdAndStatusIn(long userId, List<TaskStatus> statuses);

    @Query("SELECT t FROM Task t WHERE t.user.id = :userId " +
            "ORDER BY CASE WHEN t.status IN (com.yas.max.dto.enums.TaskStatus.PENDING, com.yas.max.dto.enums.TaskStatus.IN_PROGRESS) THEN 1 ELSE 2 END ASC, " +
            "t.createdAt DESC")
    List<Task> findTasksForUserSorted(@Param("userId") Long userId);
}
