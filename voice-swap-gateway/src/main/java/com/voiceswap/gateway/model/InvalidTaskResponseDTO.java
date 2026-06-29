package com.voiceswap.gateway.model;

import java.util.UUID;

public record InvalidTaskResponseDTO(
        UUID taskId,
        ResponseStatus status,
        String message) implements TaskResponseDTO {
}
