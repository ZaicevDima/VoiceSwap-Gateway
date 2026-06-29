package com.voiceswap.gateway.model;

import java.util.UUID;

public record ValidTaskResponseDTO(
        UUID taskId,
        ResponseStatus status,
        String message,
        String statusUrl) implements TaskResponseDTO {
}
