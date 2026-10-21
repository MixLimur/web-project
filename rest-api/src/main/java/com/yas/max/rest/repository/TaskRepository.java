package com.yas.max.rest.repository;

import com.yas.max.rest.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, String> {
}
