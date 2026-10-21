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
    @Transactional
    public void processTask(TaskMessage message) {

        log.info("Початок обробки завдання: {}", message.taskId());

        try {
            Thread.sleep(500);

            Task task = taskRepository.findById(message.taskId()).orElseThrow();
            task.setStatus(TaskStatus.IN_PROGRESS);
            taskRepository.saveAndFlush(task);

            try {
                InputStream fileStream = minioService.downloadFile(message.fileObjectName());
                byte[] content = fileStream.readAllBytes();
                log.info("  - Файл завантажено. Розмір: {} байт", content.length);
                fileStream.close();

                log.info("  - Аналіз тексту на унікальність...");
                Thread.sleep(3000);

                double similarity = Math.round(random.nextDouble(100.0) * 100.0) / 100.0;

                task.setSimilarityScore(similarity);
                task.setStatus(TaskStatus.DONE);
                taskRepository.save(task);

                log.info("Завдання {} завершено! Збігів: {}%", message.taskId(), similarity);

            } catch (Exception e) {
                log.error("Помилка обробки завдання {}: {}", message.taskId(), e.getMessage());
                task.setStatus(TaskStatus.FAILED);
                taskRepository.save(task);
            }
        } catch (Exception e) {
            log.error("Критична помилка повідомлення {}: {}", message.taskId(), e.getMessage());
        }

    }
}
