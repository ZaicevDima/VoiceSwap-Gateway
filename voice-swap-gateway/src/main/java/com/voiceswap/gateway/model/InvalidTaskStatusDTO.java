package com.voiceswap.gateway.model;

import java.util.UUID;

public record InvalidTaskStatusDTO(
        UUID taskId,
        String status,
        String progress,
        String errorMessage
) implements  TaskStatusDTO {}
