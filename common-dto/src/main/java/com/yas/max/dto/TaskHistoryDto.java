package com.yas.max.dto;

public record TaskHistoryDto(
        String taskId,
        String fileName,
        String status,
        Integer progress,
        String similarityScore
) {
}
