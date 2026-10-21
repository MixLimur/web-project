package com.yas.max.worker.service;

import com.yas.max.dto.messages.TaskMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskListener {

    @RabbitListener(queues = "task.queue")
    public void processTask(TaskMessage message) {
        log.info("Отримано завдання з RabbitMQ");
        log.info("  - Task ID: {}", message.taskId());
        log.info("  - User ID: {}", message.userId());
        log.info("  - File Name: {}", message.fileObjectName());

        try {
            //do work
            Thread.sleep(2000);
        } catch (Exception e) {
            log.error("Помилка обробки завдання {}: {}", message.taskId(), e.getMessage());
        }
    }
}
