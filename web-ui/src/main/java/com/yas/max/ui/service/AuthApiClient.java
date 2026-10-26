package com.yas.max.ui.service;

import com.yas.max.dto.auth.RegistrationRequestDto;
import com.yas.max.dto.auth.UserAuthDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class AuthApiClient {
    private final RestClient restClient;

    public UserAuthDto getUserByUsername(String username) {
        return restClient.get()
                .uri("/api/auth/user/" + username)
                .retrieve()
                .body(UserAuthDto.class);
    }

    public void registerUser(RegistrationRequestDto requestDto) {
        restClient.post()
                .uri("/api/auth/register")
                .body(requestDto)
                .retrieve()
                .toBodilessEntity();
    }
}
