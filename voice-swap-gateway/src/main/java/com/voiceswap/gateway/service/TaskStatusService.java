package com.voiceswap.gateway.service;

import com.voiceswap.gateway.model.TaskStatusDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TaskStatusService {

    Logger log = LoggerFactory.getLogger(TaskStatusService.class);
    private final Map<UUID, TaskStatusDTO> tasksStatus = new ConcurrentHashMap<>();

    public TaskStatusDTO getTaskStatus(UUID taskId) {
        TaskStatusDTO status = tasksStatus.get(taskId);

        if (status == null) {
            log.warn("Task not found: {}", taskId); // TODO сделать исключением
        }
        return status;
    }

    public void updateStatus(TaskStatusDTO taskStatusDTO) {
        tasksStatus.put(taskStatusDTO.taskId(), taskStatusDTO);
        log.info("Task {}: {} ({})", taskStatusDTO.taskId(), taskStatusDTO.status(), taskStatusDTO.progress());
    }
}
