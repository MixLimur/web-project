package com.yas.max.ui.controller;

import com.yas.max.ui.service.TaskApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final TaskApiClient taskApiClient;

    @GetMapping("/admin")
    public String adminPanel(Model model) {
        model.addAttribute("activeTasks", taskApiClient.getActiveTasks());
        return "admin";
    }

    @PostMapping("/admin/cancel/{id}")
    @ResponseBody
    public void cancelTaskAsAdmin(@PathVariable("id") String taskId, Principal principal) {
        if (principal != null && ADMIN_USERNAME.equals(principal.getName())) {
            taskApiClient.adminCancelTask(taskId);
        }
    }
}
