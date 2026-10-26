package com.yas.max.rest.controller;

import com.yas.max.dto.auth.RegistrationRequestDto;
import com.yas.max.dto.auth.UserAuthDto;
import com.yas.max.rest.entity.User;
import com.yas.max.rest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegistrationRequestDto request) {
        if (userRepository.findByIdentifier(request.username()).isPresent()) {
            return ResponseEntity.badRequest().body("Користувач з таким логіном вже існує");
        }

        User newUser = User.builder()
                .identifier(request.username())
                .password(passwordEncoder.encode(request.password()))
                .role("ROLE_USER")
                .isRegistered(true)
                .filesProcessed(0)
                .build();

        userRepository.save(newUser);
        return ResponseEntity.ok("Реєстрація успішна");
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<UserAuthDto> getUserByUsername(@PathVariable String username) {
        return userRepository.findByIdentifier(username)
                .filter(User::isRegistered)
                .map(u -> new UserAuthDto(u.getIdentifier(), u.getPassword(), u.getRole()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
