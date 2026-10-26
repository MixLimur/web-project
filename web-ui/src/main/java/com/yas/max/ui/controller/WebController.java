package com.yas.max.ui.controller;

import com.yas.max.dto.TaskHistoryDto;
import com.yas.max.ui.service.TaskApiClient;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final TaskApiClient taskApiClient;

    @GetMapping("/")
    public String index(Principal principal, HttpSession session, Model model) {
        boolean isRegistered = (principal != null);
        String identifier = isRegistered ? principal.getName() : "guest_" + session.getId();

        try {
            List<?> tasks = taskApiClient.getUserTasksHistory(identifier);
            model.addAttribute("tasks", tasks);
        } catch (Exception e) {
            model.addAttribute("tasks", List.of());
        }

        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,
                             Principal principal,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        try {
            boolean isRegistered = (principal != null);
            String identifier = isRegistered ? principal.getName() : "guest_" + session.getId();

            String taskId = taskApiClient.uploadTask(file.getResource(), identifier, isRegistered);

            redirectAttributes.addFlashAttribute("taskId", taskId);
            redirectAttributes.addFlashAttribute("message", "Завдання створено! ID: " + taskId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Помилка зв'язку з API: " + e.getMessage());
        }
        return "redirect:/";
    }

    @GetMapping(value = "/history-fragment")
    public String getHistoryData(Principal principal, HttpSession session, Model model) {
        boolean isRegistered = (principal != null);
        String identifier = isRegistered ? principal.getName() : "guest_" + session.getId();
        try {
            model.addAttribute("tasks", taskApiClient.getUserTasksHistory(identifier));
        } catch (Exception e) {
            model.addAttribute("tasks", List.of());
        }
        return "index :: history-tbody";
    }

    @PostMapping(value = "/cancel/{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> cancelTaskUI(@PathVariable String taskId, Principal principal, HttpSession session) {
        boolean isRegistered = (principal != null);
        String identifier = isRegistered ? principal.getName() : "guest_" + session.getId();

        try {
            String cleanTaskId = taskId.replaceAll("\"", "");
            taskApiClient.cancelTask(cleanTaskId, identifier);
            return ResponseEntity.ok(Map.of("status", "SUCCESS"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Не вдалося скасувати"));
        }
    }

    @GetMapping("/status-fragment/{taskId}")
    public String getStatusFragment(@PathVariable String taskId, Model model) {
        try {
            String cleanTaskId = taskId.replaceAll("\"", "");

            TaskHistoryDto statusData = taskApiClient.getTaskStatus(cleanTaskId);

            model.addAttribute("taskId", cleanTaskId);
            model.addAttribute("statusData", statusData);
        } catch (Exception e) {
            model.addAttribute("taskId", taskId);
            model.addAttribute("statusData", null);
        }
        return "index :: status-block";
    }

}
