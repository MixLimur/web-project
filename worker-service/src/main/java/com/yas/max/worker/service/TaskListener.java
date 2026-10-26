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
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskListener {

    private final MinioService minioService;
    private final TaskRepository taskRepository;
    private final Random random = new Random();

    @RabbitListener(queues = "task.queue")
    public void processTask(TaskMessage message) {

        log.info("Початок обробки завдання: {}", message.taskId());

        try {
            Thread.sleep(500);

            Task task = taskRepository.findById(message.taskId())
                    .orElseThrow(() -> new RuntimeException("Завдання не знайдено в БД!"));

            if (task.getStatus() == TaskStatus.CANCELLED) {
                System.out.println("Завдання " + task.getId() + " скасовано користувачем.");
                return;
            }

            task.setStatus(TaskStatus.IN_PROGRESS);
            task.setProgress(0);
            taskRepository.save(task);

            try {
                InputStream fileStream = minioService.downloadFile(message.fileObjectName());
                byte[] content = fileStream.readAllBytes();
                log.info("  - Файл завантажено. Розмір: {} байт", content.length);
                fileStream.close();

                log.info("  - Аналіз тексту на унікальність...");
                Thread.sleep(3000);

                for (int i = 20; i <= 80; i += 20) {

                    Thread.sleep(3000);

                    Task freshTask = taskRepository.findById(message.taskId()).orElse(null);

                    if (freshTask == null || freshTask.getStatus() == TaskStatus.CANCELLED) {
                        log.info("Завдання {} скасовано під час виконання! Зупинка.", message.taskId());
                        return;
                    }
                    freshTask.setProgress(i);
                    taskRepository.saveAndFlush(freshTask);
                    log.info("  - Прогрес: {}%", i);
                }

                Task finalTask = taskRepository.findById(message.taskId()).orElse(null);
                if (finalTask == null || finalTask.getStatus() == TaskStatus.CANCELLED) {
                    log.info("Завдання {} скасовано перед видачею результату.", message.taskId());
                    return;
                }

                double similarity = Math.round(random.nextDouble(100.0) * 100.0) / 100.0;

                finalTask.setSimilarityScore(similarity);
                finalTask.setProgress(100);
                finalTask.setStatus(TaskStatus.DONE);
                taskRepository.save(finalTask);

                log.info("Завдання {} завершено! Збігів: {}%", message.taskId(), similarity);

            } catch (Exception e) {
                log.error("Помилка обробки завдання {}: {}", message.taskId(), e.getMessage());
                taskRepository.findById(message.taskId()).ifPresent(t -> {
                    if (t.getStatus() != TaskStatus.CANCELLED) {
                        t.setStatus(TaskStatus.FAILED);
                        taskRepository.save(t);
                    }
                });
            }
        } catch (Exception e) {
            log.error("Критична помилка повідомлення {}: {}", message.taskId(), e.getMessage());
        }

    }
}
