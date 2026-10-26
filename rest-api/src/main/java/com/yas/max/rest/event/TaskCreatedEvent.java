package com.yas.max.rest.event;

import com.yas.max.dto.messages.TaskMessage;

public record TaskCreatedEvent(TaskMessage taskMessage) {
}
