package com.voiceswap.gateway.model;

import java.util.UUID;

public record InvalidTaskStatusDTO(
        UUID taskId,
        String errorMessage
) implements  TaskStatusDTO {
    @Override
    public TaskStatus status() { return TaskStatus.FAILED; }
    @Override
    public String progress() { return "0%"; }
}
