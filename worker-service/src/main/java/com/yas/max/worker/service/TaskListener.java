package com.yas.max.worker.service;

import com.yas.max.dto.messages.TaskMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskListener {

    private final MinioService minioService;

    @RabbitListener(queues = "task.queue")
    public void processTask(TaskMessage message) {
        log.info("Отримано завдання з RabbitMQ");
        log.info("  - Task ID: {}", message.taskId());
        log.info("  - User ID: {}", message.userId());
        log.info("  - File Name: {}", message.fileObjectName());

        try {
            InputStream fileStream = minioService.downloadFile(message.fileObjectName());

            byte[] content = fileStream.readAllBytes();

            fileStream.close();

            log.info("Обробка документу");
            Thread.sleep(5000);
            log.info("Документ успішно оброблено");
        } catch (Exception e) {
            log.error("Помилка обробки завдання {}: {}", message.taskId(), e.getMessage());
        }

    }
}
