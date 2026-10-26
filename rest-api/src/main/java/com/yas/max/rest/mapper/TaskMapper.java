package com.yas.max.rest.mapper;

import com.yas.max.dto.TaskHistoryDto;
import com.yas.max.rest.entity.Task;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class TaskMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public TaskHistoryDto toHistoryDto(Task task) {

        String fullFileName = task.getFileObjectName();
        String cleanFileName = fullFileName;

        int underscoreIndex = fullFileName.indexOf('_');
        if (underscoreIndex != -1) {
            cleanFileName = fullFileName.substring(underscoreIndex + 1);
        }

        String formattedDate = task.getCreatedAt() != null
                ? task.getCreatedAt().format(FORMATTER)
                : "—";

        return new TaskHistoryDto(
                task.getId(),
                cleanFileName,
                task.getStatus().name(),
                task.getProgress() != null ? task.getProgress() : 0,
                task.getSimilarityScore() != null ? task.getSimilarityScore().toString() : "—",
                formattedDate
        );
    }
}
