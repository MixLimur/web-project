package com.yas.max.dto;

import java.time.LocalDateTime;

public record TaskHistoryDto(
        String taskId,
        String fileName,
        String status,
        Integer progress,
        String similarityScore,
        String createdAt,
        Integer estimatedWaitTimeSeconds
) {
}
