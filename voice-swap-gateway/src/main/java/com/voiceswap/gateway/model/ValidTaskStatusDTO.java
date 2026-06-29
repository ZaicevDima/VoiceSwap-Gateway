package com.voiceswap.gateway.model;

import java.util.UUID;

public record ValidTaskStatusDTO(
        UUID taskId,
        TaskStatus status,
        String progress,
        String resultUrl
) implements TaskStatusDTO {}
