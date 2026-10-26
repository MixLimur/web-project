package com.yas.max.dto.auth;

public record UserAuthDto(
        String username,
        String passwordHash,
        String role
) {
}
