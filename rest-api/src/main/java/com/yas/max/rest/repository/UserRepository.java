package com.yas.max.rest.repository;

import com.yas.max.rest.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByIdentifier(String identifier);
}
