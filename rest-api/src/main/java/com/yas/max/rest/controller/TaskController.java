package com.yas.max.rest.controller;

import com.yas.max.dto.TaskHistoryDto;
import com.yas.max.rest.entity.Task;
import com.yas.max.rest.exception.QuotaExceededException;
import com.yas.max.rest.exception.TaskNotFoundException;
import com.yas.max.rest.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("identifier") String identifier,
            @RequestParam(value = "isRegistered", defaultValue = "false") boolean isRegistered) {

        try {
            String taskId = taskService.processFile(file, identifier, isRegistered);
            return ResponseEntity.ok(taskId);
        } catch (QuotaExceededException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Помилка обробки файлу: " + e.getMessage());
        }
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<?> getTaskStatus(@PathVariable String taskId) {
        try {
            TaskHistoryDto taskDto = taskService.getTaskDto(taskId);
            return ResponseEntity.ok(taskDto);
        } catch (TaskNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Системна помилка"));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<TaskHistoryDto>> getUserTasks(@RequestParam("identifier") String identifier) {
        return ResponseEntity.ok(taskService.getUserTasks(identifier));
    }

    @PostMapping("/{taskId}/cancel")
    public ResponseEntity<?> cancelTask(@PathVariable String taskId, @RequestParam("identifier") String identifier) {
        System.out.println("Trying to stop task");
        try {
            taskService.cancelTask(taskId, identifier);
            return ResponseEntity.ok(Map.of("message", "Завдання скасовано"));
        } catch (TaskNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}
