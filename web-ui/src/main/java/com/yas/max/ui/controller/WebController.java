package com.yas.max.ui.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final RestClient restClient;

    @GetMapping("/")
    public String index() {
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
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", file.getResource());

            boolean isRegistered = (principal != null);
            String identifier = isRegistered ? principal.getName() : "guest_" + session.getId();
            body.add("identifier", identifier);
            body.add("isRegistered", isRegistered);

            String taskId = restClient.post()
                    .uri("api/tasks/upload")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            redirectAttributes.addFlashAttribute("message", "Завдання створено! ID: " + taskId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Помилка зв'язку з API: " + e.getMessage());
        }
        return "redirect:/";
    }
}
