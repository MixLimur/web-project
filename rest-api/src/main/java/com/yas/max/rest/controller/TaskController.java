package com.yas.max.rest.controller;

import com.yas.max.rest.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("identifier") String identifier,
            @RequestParam(value = "isRegistered", defaultValue = "fasle") boolean isRegistered) {

        try {
            String taskId = taskService.processFile(file, identifier, isRegistered);
            return ResponseEntity.ok(taskId);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Помилка обробки файлу: " + e.getMessage());
        }

    }
}
