package com.voiceswap.gateway.service;

import com.voiceswap.gateway.model.TaskStatusDTO;
import com.voiceswap.gateway.model.ValidTaskStatusDTO;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TaskStatusService {
    public TaskStatusDTO getTaskStatus(UUID taskId) {
        return new ValidTaskStatusDTO(taskId, "OK", "", "", "");
    }
}
