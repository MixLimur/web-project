package com.yas.max.ui.controller;

import com.yas.max.ui.service.AuthApiClient;
import com.yas.max.ui.service.TaskApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final TaskApiClient taskApiClient;
    private final AuthApiClient authApiClient;

    @GetMapping("/admin")
    public String adminPanel(Principal principal, Model model) {
        model.addAttribute("activeTasks", taskApiClient.getActiveTasks());
        return "admin";
    }

    @PostMapping("/admin/cancel/{id}")
    @ResponseBody
    public void cancelTaskAsAdmin(@PathVariable("id") String taskId, Principal principal) {
        taskApiClient.adminCancelTask(taskId);
    }
}
