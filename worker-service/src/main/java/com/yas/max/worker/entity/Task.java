package com.yas.max.worker.entity;

import com.yas.max.dto.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    private String fileObjectName;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "similarity_score")
    private Double similarityScore;
}
