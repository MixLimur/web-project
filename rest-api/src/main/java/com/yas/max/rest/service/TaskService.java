package com.yas.max.rest.service;

import com.yas.max.dto.enums.TaskStatus;
import com.yas.max.dto.messages.TaskMessage;
import com.yas.max.rest.config.RabbitMQConfig;
import com.yas.max.rest.entity.Task;
import com.yas.max.rest.entity.User;
import com.yas.max.rest.repository.TaskRepository;
import com.yas.max.rest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final MinioService minioService;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public String processFile(MultipartFile file, String identifier, boolean isRegistered) throws Exception {

        User user = getOrCreateUser(identifier, isRegistered);
        validateUserQuota(user);

        String taskId = UUID.randomUUID().toString();
        String fileObjectName = taskId + "_" + file.getOriginalFilename();

        minioService.uploadFile(file, fileObjectName);
        updateQuotaAndSaveTask(user, taskId, fileObjectName);
        sendTaskToQueue(taskId, user.getId(), fileObjectName);

        return taskId;
    }

    private User getOrCreateUser(String identifier, boolean isRegistered) {
        return userRepository.findByIdentifier(identifier)
                .orElseGet(() -> userRepository.save(User.builder()
                        .identifier(identifier)
                        .isRegistered(isRegistered)
                        .filesProcessed(0)
                        .build()));
    }

    private void validateUserQuota(User user) {
        int limit = user.isRegistered() ? 10 : 3;
        if (user.getFilesProcessed() >= limit) {
            throw new RuntimeException("Вичерпано ліміт перевірок для користувача: " + user.getIdentifier());
        }
    }

    private void updateQuotaAndSaveTask(User user, String taskId, String fileObjectName) {
        user.setFilesProcessed(user.getFilesProcessed() + 1);
        userRepository.save(user);

        Task task = Task.builder()
                .id(taskId)
                .status(TaskStatus.PENDING)
                .fileObjectName(fileObjectName)
                .user(user)
                .build();
        taskRepository.save(task);
    }

    private void sendTaskToQueue(String taskId, Long userId, String fileObjectName) {
        TaskMessage message = new TaskMessage(taskId, userId, fileObjectName);
        rabbitTemplate.convertAndSend(RabbitMQConfig.TASK_EXCHANGE, RabbitMQConfig.TASK_ROUTING_KEY, message);
    }
}
