package com.yas.max.rest.repository;

import com.yas.max.dto.enums.TaskStatus;
import com.yas.max.rest.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, String> {
    long countByUserIdAndStatusIn(long userId, List<TaskStatus> statuses);
}
