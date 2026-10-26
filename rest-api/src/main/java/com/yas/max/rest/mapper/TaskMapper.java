package com.yas.max.rest.mapper;

import com.yas.max.dto.TaskHistoryDto;
import com.yas.max.rest.entity.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskHistoryDto toHistoryDto(Task task) {
        return new TaskHistoryDto(
                task.getId(),
                task.getFileObjectName(),
                task.getStatus().name(),
                task.getProgress() != null ? task.getProgress() : 0,
                task.getSimilarityScore() != null ? task.getSimilarityScore().toString() : "—"
        );
    }
}
