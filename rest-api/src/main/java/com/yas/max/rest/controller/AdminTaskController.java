package com.yas.max.rest.controller;

import com.yas.max.dto.TaskHistoryDto;
import com.yas.max.rest.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/admin")
@RequiredArgsConstructor
public class AdminTaskController {

    private final TaskService taskService;

    @GetMapping("/tasks/active")
    public List<TaskHistoryDto> getActiveTasks() {
        return taskService.getAllActiveTasks();
    }

    @PostMapping("/tasks/{taskId}/cancel")
    public void cancelTask(@PathVariable String taskId) {
        taskService.adminCancelTask(taskId);
    }
}
