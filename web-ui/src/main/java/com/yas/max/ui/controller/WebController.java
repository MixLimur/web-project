package com.yas.max.ui.controller;

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

    @GetMapping(value = "/status/{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> getTaskStatus(@PathVariable String taskId) {
        try {
            String cleanTaskId = taskId.replaceAll("\"", "");
            String response = taskApiClient.getTaskStatus(cleanTaskId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\": \"Помилка отримання статусу\"}");
        }
    }

    @GetMapping(value = "/history-data", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> getHistoryData(Principal principal, HttpSession session) {
        boolean isRegistered = (principal != null);
        String identifier = isRegistered ? principal.getName() : "guest_" + session.getId();
        try {
            return ResponseEntity.ok(taskApiClient.getUserTasksHistory(identifier));
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }
}
