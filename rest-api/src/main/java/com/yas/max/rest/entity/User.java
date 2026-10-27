package com.yas.max.rest.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String identifier;

    private String password;

    @Builder.Default
    @Column(nullable = false)
    private String role = "ROLE_USER";

    @Column(nullable = false)
    private boolean isRegistered;

    @Column(nullable = false)
    private int filesProcessed;


}
