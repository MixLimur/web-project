package com.yas.max.dto.messages;

import java.io.Serializable;

public record TaskMessage(
        String taskId,
        Long userId,
        String fileObjectName
) implements Serializable {}
