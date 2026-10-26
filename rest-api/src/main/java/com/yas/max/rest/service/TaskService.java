package com.yas.max.rest.service;

import com.yas.max.dto.TaskHistoryDto;
import com.yas.max.dto.enums.TaskStatus;
import com.yas.max.dto.messages.TaskMessage;
import com.yas.max.rest.config.RabbitMQConfig;
import com.yas.max.rest.entity.Task;
import com.yas.max.rest.entity.User;
import com.yas.max.rest.event.TaskCreatedEvent;
import com.yas.max.rest.exception.QuotaExceededException;
import com.yas.max.rest.exception.TaskNotFoundException;
import com.yas.max.rest.listener.TaskTransactionListener;
import com.yas.max.rest.mapper.TaskMapper;
import com.yas.max.rest.repository.TaskRepository;
import com.yas.max.rest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final MinioService minioService;
    private final ApplicationEventPublisher eventPublisher;
    private final TaskMapper taskMapper;

    @Value("${app.limits.guest-active-tasks}")
    private int guestLimit;

    @Value("${app.limits.user-active-tasks}")
    private int userLimit;

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

    @Transactional(readOnly = true)
    public Task getTask(String taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Завдання не знайдено"));
    }

    @Transactional(readOnly = true)
    public TaskHistoryDto getTaskDto(String taskId) {
        Task task = getTask(taskId);
        return taskMapper.toHistoryDto(task);
    }

    private User getOrCreateUser(String identifier, boolean isRegistered) {
        return userRepository.findByIdentifier(identifier)
                .orElseGet(() -> userRepository.save(User.builder()
                        .identifier(identifier)
                        .isRegistered(isRegistered)
                        .filesProcessed(0)
                        .build()));
    }

    @Transactional(readOnly = true)
    private void validateUserQuota(User user) {
        int currentLimit = user.isRegistered() ? userLimit : guestLimit;

        long activeTasks = taskRepository.countByUserIdAndStatusIn(
                user.getId(),
                List.of(TaskStatus.PENDING, TaskStatus.IN_PROGRESS)
        );

        if (activeTasks >= currentLimit) {
            String role = user.isRegistered() ? "користувачів" : "гостей";
            throw new QuotaExceededException(
                    String.format("Вичерпано ліміт одночасних перевірок для %s (%d завдань). Дочекайтесь завершення поточних.", role, currentLimit)
            );
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
        eventPublisher.publishEvent(new TaskCreatedEvent(message));
    }

    @Transactional(readOnly = true)
    public List<TaskHistoryDto> getUserTasks(String identifier) {
        return userRepository.findByIdentifier(identifier)
                .map(user -> taskRepository.findAllByUserId(user.getId()).stream()
                        .map(taskMapper::toHistoryDto)
                        .sorted(java.util.Comparator.comparing((TaskHistoryDto dto) ->
                            (dto.status().equals("PENDING") || dto.status().equals("IN_PROGRESS")) ? 0 : 1
                        ))
                        .toList())
                .orElse(List.of());
    }

    public void cancelTask(String taskId, String identifier) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Завдання не знайдено"));
        if(!task.getUser().getIdentifier().equals(identifier)) {
            throw new RuntimeException("Немає доступу до скасування цього завдання");
        }
        if (task.getStatus() == TaskStatus.DONE || task.getStatus() == TaskStatus.CANCELLED) {
            throw new RuntimeException("Неможливо скасувати завдання в поточному статусі");
        }

        task.setStatus(TaskStatus.CANCELLED);
        taskRepository.save(task);
        System.out.println("Task"+ taskId +" cancelled");
    }
}
