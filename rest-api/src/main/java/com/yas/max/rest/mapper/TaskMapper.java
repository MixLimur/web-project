package com.yas.max.rest.mapper;

import com.yas.max.dto.TaskHistoryDto;
import com.yas.max.dto.enums.TaskStatus;
import com.yas.max.rest.entity.Task;
import com.yas.max.rest.repository.TaskRepository;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;

@Component
@AllArgsConstructor
public class TaskMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private final TaskRepository taskRepository;
    private final AmqpAdmin amqpAdmin;

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

        Integer waitTime = calcWaitingTime(task);


        return new TaskHistoryDto(
                task.getId(),
                cleanFileName,
                task.getStatus().name(),
                task.getProgress() != null ? task.getProgress() : 0,
                task.getSimilarityScore() != null ? task.getSimilarityScore().toString() : "—",
                formattedDate,
                waitTime
        );
    }

    private @Nullable Integer calcWaitingTime(Task task) {
        Integer waitTime = null;
        double baseSecondsPerKb = 8.5;
        if (task.getStatus() == TaskStatus.PENDING) {

            List<Task> inProgressTasks = taskRepository.findByStatus(TaskStatus.IN_PROGRESS);

            long remainingInProgressBytes = inProgressTasks.stream()
                    .mapToLong(TaskMapper::getRemainingBytesToProcess)
                    .sum();
            long pendingBytesAhead = taskRepository.sumFileSizeAhead(TaskStatus.PENDING, task.getCreatedAt());

            double totalKbToProcess = (double) (remainingInProgressBytes + pendingBytesAhead) / 1024;
            double dynamicSecondsPerKb = baseSecondsPerKb + (totalKbToProcess * 0.01);

            int activeWorkers = getActiveWorkers();

            waitTime = (int) Math.ceil((totalKbToProcess * dynamicSecondsPerKb) / activeWorkers);
        }
        if(task.getStatus() == TaskStatus.IN_PROGRESS) {
            long remainingBytes = getRemainingBytesToProcess(task);
            long remainingKB = remainingBytes / 1024;
            double dynamicSecondsPerKb = baseSecondsPerKb + (remainingKB * 0.01);
            waitTime = (int) (remainingKB * dynamicSecondsPerKb);
        }
        return waitTime;
    }

    private static long getRemainingBytesToProcess(Task task)
    {
        long size = task.getFileSize() != null ? task.getFileSize() : 0L;
        int progress = task.getProgress() != null ? task.getProgress() : 0;
        double remainingPercent = (100.0 - progress) / 100.0;
        return (long) (size * remainingPercent);
    }

    private int getActiveWorkers() {
        int activeWorkers = 1;
        try {
            Properties queueProps = amqpAdmin.getQueueProperties("task.queue");
            if (queueProps != null && queueProps.get(RabbitAdmin.QUEUE_CONSUMER_COUNT) != null) {
                activeWorkers = (Integer) queueProps.get(RabbitAdmin.QUEUE_CONSUMER_COUNT);
            }
        } catch (Exception e) {}
        if (activeWorkers < 1) activeWorkers = 1;
        return activeWorkers;
    }

}
