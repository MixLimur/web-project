package com.yas.max.rest.controller;

import com.yas.max.rest.entity.Task;
import com.yas.max.rest.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
            @RequestParam(value = "isRegistered", defaultValue = "fals  e") boolean isRegistered) {

        try {
            String taskId = taskService.processFile(file, identifier, isRegistered);
            return ResponseEntity.ok(taskId);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Помилка обробки файлу: " + e.getMessage());
        }
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<?> getTaskStatus(@PathVariable String taskId) {
        Task task = taskService.getTask(taskId);
        return ResponseEntity.ok(Map.of(
                "taskId", task.getId(),
                "status", task.getStatus().name(),
                "progress", task.getProgress() != null ? task.getProgress() : 0,
                "similarityScore", task.getSimilarityScore() != null ? task.getSimilarityScore() : "Ще перевіряється"
                )
        );
    }
}
