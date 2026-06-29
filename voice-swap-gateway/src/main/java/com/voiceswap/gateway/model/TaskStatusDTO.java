package com.voiceswap.gateway.model;

import java.util.UUID;

public sealed interface TaskStatusDTO permits ValidTaskStatusDTO, InvalidTaskStatusDTO {
    UUID taskId();
    TaskStatus status();
    String progress();
}
