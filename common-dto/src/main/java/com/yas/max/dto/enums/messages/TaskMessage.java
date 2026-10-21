package com.yas.max.dto.enums.messages;

import java.io.Serializable;

public record TaskMessage(
        String taskId,
        Long userId,
        String fileObjectName
) implements Serializable {}
