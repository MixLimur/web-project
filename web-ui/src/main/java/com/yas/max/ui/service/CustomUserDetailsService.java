package com.yas.max.ui.service;

import com.yas.max.dto.auth.UserAuthDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AuthApiClient authApiClient;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            UserAuthDto userDto = authApiClient.getUserByUsername(username);

            return User.builder()
                    .username(userDto.username())
                    .password(userDto.passwordHash())
                    .authorities(userDto.role())
                    .build();
        } catch (Exception e) {
            throw new UsernameNotFoundException("Користувача не знайдено: " + username, e);
        }
    }
}
