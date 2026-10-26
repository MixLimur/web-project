package com.yas.max.ui.controller;

import com.yas.max.dto.auth.RegistrationRequestDto;
import com.yas.max.ui.service.AuthApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthApiClient authApiClient;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(
            @RequestParam String username,
            @RequestParam String password,
            Model model) {

        try {
            RegistrationRequestDto request = new RegistrationRequestDto(username, password);
            authApiClient.registerUser(request);
            return "redirect:/login?registered=true";
        } catch (Exception e) {
            model.addAttribute("error", "Помилка реєстрації. Можливо, користувач вже існує.");
            return "register";
        }
    }

    @GetMapping("/error")
    public String error() {
        return "error";
    }
}
