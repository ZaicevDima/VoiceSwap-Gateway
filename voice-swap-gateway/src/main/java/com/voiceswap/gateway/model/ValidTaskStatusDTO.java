package com.voiceswap.gateway.model;

import java.util.UUID;

public record ValidTaskStatusDTO(
        UUID taskId,
        String status,
        String progress,
        String resultUrl,
        String errorMessage
) implements TaskStatusDTO {}
