package com.yas.max.worker.service;

import com.yas.max.dto.enums.TaskStatus;
import com.yas.max.dto.messages.TaskMessage;
import com.yas.max.worker.entity.Task;
import com.yas.max.worker.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.OptionalDouble;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskListener {

    private final MinioService minioService;
    private final TaskRepository taskRepository;
    private final PlagiarismSimulatorService simulatorService;
    private final Random random = new Random();

    @RabbitListener(queues = "task.queue")
    public void processTask(TaskMessage message) {
        String taskId = message.taskId();
        log.info("Початок обробки завдання: {}", taskId);

        try {
            if (!initializeTask(taskId)) {
                return;
            }

            OptionalDouble similarityResult = executeHeavyLogic(message);

            if (similarityResult.isPresent()) {
                saveTaskResult(taskId, similarityResult.getAsDouble());
            } else {
                log.info("Завдання {} перервано під час виконання.", taskId);
            }

        } catch (Exception e) {
            log.error("Помилка обробки завдання {}: {}", taskId, e.getMessage());
            markTaskAsFailed(taskId);
        }
    }

    private boolean initializeTask(String taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Завдання не знайдено в БД!"));

        if (task.getStatus() == TaskStatus.CANCELLED) {
            log.info("Завдання {} скасовано користувачем.", taskId);
            return false;
        }

        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setProgress(0);
        taskRepository.save(task);
        return true;
    }

    private OptionalDouble executeHeavyLogic(TaskMessage message) throws Exception {
        String taskId = message.taskId();

        log.info("  - Завантаження та читання файлу...");
        String text = minioService.getFileContent(message.fileObjectName());

        log.info("  - Початок важкого CPU-аналізу...");
        double similarity = simulatorService.performHeavyAnalysis(
                text,
                progress -> updateTaskProgress(taskId, progress),
                () -> isTaskCancelled(taskId)
        );

        if (similarity < 0) {
            return OptionalDouble.empty(); // Сигналізуємо, що розрахунок перервано
        }

        return OptionalDouble.of(similarity);
    }

    private void updateTaskProgress(String taskId, int progress) {
        if (progress % 5 == 0) {
            taskRepository.findById(taskId).ifPresent(t -> {
                if (t.getStatus() != TaskStatus.CANCELLED) {
                    t.setProgress(progress);
                    taskRepository.save(t);
                    log.info("  - Прогрес завдання: {}%", progress);
                }
            });
        }
    }

    private boolean isTaskCancelled(String taskId) {
        return taskRepository.findById(taskId)
                .map(t -> t.getStatus() == TaskStatus.CANCELLED)
                .orElse(true);
    }

    private void saveTaskResult(String taskId, double similarity) {
        taskRepository.findById(taskId).ifPresent(task -> {
            if (task.getStatus() != TaskStatus.CANCELLED) {
                task.setSimilarityScore(similarity);
                task.setProgress(100);
                task.setStatus(TaskStatus.DONE);
                taskRepository.save(task);
                log.info("Завдання {} завершено! Збігів: {}%", taskId, similarity);
            }
        });
    }

    private void markTaskAsFailed(String taskId) {
        taskRepository.findById(taskId).ifPresent(task -> {
            if (task.getStatus() != TaskStatus.CANCELLED) {
                task.setStatus(TaskStatus.FAILED);
                taskRepository.save(task);
            }
        });
    }

}
