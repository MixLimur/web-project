package com.yas.max.ui.controller;

import com.yas.max.dto.TaskHistoryDto;
import com.yas.max.ui.service.TaskApiClient;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientResponseException;
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
        String identifier = resolveIdentifier(principal, session);

        model.addAttribute("userName", principal != null ? principal.getName() : "Гість");

        try {
            List<?> tasks = taskApiClient.getUserTasksHistory(identifier);
            model.addAttribute("tasks", tasks);
        } catch (Exception e) {
            model.addAttribute("tasks", List.of());
        }

        return "index";
    }


    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,
                             Principal principal,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        try {
            String identifier = resolveIdentifier(principal, session);
            boolean isRegistered = (principal != null);

            String taskId = taskApiClient.uploadTask(file.getResource(), identifier, isRegistered);

            redirectAttributes.addFlashAttribute("taskId", taskId);
            redirectAttributes.addFlashAttribute("message", "Завдання створено!");
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                redirectAttributes.addFlashAttribute("error", e.getResponseBodyAsString());
            } else {
                System.err.println("Серверна помилка API: " + e.getMessage());
                redirectAttributes.addFlashAttribute("error", "Виникла помилка при обробці файлу. Будь ласка, спробуйте пізніше.");
            }
        } catch (Exception e) {
            System.err.println("Внутрішня помилка UI: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Виникла помилка при обробці файлу. Будь ласка, спробуйте пізніше.");
        }
        return "redirect:/";
    }

    @GetMapping(value = "/history-fragment")
    public String getHistoryFragment(Principal principal, HttpSession session, Model model) {
        String identifier = resolveIdentifier(principal, session);
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
        String identifier = resolveIdentifier(principal, session);

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

    private String resolveIdentifier(Principal principal, HttpSession session) {
        return (principal != null) ? principal.getName() : "guest_" + session.getId();
    }

}
