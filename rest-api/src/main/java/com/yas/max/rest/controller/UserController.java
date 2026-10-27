package com.yas.max.rest.controller;

import com.yas.max.rest.entity.User;
import com.yas.max.rest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/{identifier}/role")
    public String getUserRole(@PathVariable String identifier) {
        return userRepository.findByIdentifier(identifier)
                .map(User::getRole)
                .orElse("USER");
    }

}
