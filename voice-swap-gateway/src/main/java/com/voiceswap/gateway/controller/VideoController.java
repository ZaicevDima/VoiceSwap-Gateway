package com.voiceswap.gateway.controller;

import com.voiceswap.gateway.exceptions.FileException;
import com.voiceswap.gateway.model.*;
import com.voiceswap.gateway.model.ResponseStatus;
import com.voiceswap.gateway.service.OrchestrationService;
import com.voiceswap.gateway.service.TaskStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

@RestController
@RequestMapping("api/v1/video")
public class VideoController {

    private static final Logger log = LoggerFactory.getLogger(VideoController.class);

    private final OrchestrationService orchestrationService;
    private final TaskStatusService taskStatusService;

    public VideoController(OrchestrationService orchestrationService, TaskStatusService taskStatusService) {
        this.orchestrationService = orchestrationService;
        this.taskStatusService = taskStatusService;
    }

    @PostMapping
    public ResponseEntity<TaskResponseDTO> voiceSwap(@RequestParam("video") MultipartFile videoFile,
                                                     @RequestParam("voiceSample") MultipartFile voiceSampleFile) {

        UUID taskId = UUID.randomUUID();

        try {
            validateFiles(videoFile, voiceSampleFile);

            log.info("Started new task with id: {}", taskId);

            String videoPath = saveFile(videoFile, taskId, "video");
            String voicePath = saveFile(voiceSampleFile, taskId, "voice");

            orchestrationService.performVoiceSwap(taskId, videoPath, voicePath);

            TaskResponseDTO taskResponseDTO = buildTaskResponseDTO(taskId);
            return ResponseEntity.accepted().body(taskResponseDTO);

        } catch (IOException exception) {
            String errorMessage = "Ошибка при сохранении файлов: " + exception.getMessage();
            log.error("❌ {}", errorMessage);

            return buildErrorResponse(taskId, errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception exception) {
            String errorMessage = "Ошибка во время процесса подмены аудиодорожки: " + exception.getMessage();
            log.error("❌ {}", errorMessage);

            return buildErrorResponse(
                    taskId,
                    errorMessage,
                    HttpStatus.BAD_REQUEST);
        }
    }

    private static void validateFiles(MultipartFile videoFile, MultipartFile voiceSampleFile) {
        Objects.requireNonNull(videoFile, "video file mustn't be null");
        Objects.requireNonNull(voiceSampleFile, "voice sample file mustn't be null");

        List<String> emptyFiles = Stream.of(
                        new AbstractMap.SimpleEntry<>("video", videoFile),
                        new AbstractMap.SimpleEntry<>("voiceSample", voiceSampleFile))
                .filter(entry -> entry.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .toList();

        if (!emptyFiles.isEmpty()) {
            throw new FileException("Files mustn't be empty: " + String.join(", ", emptyFiles));
        }
    }

    @GetMapping("/status/{taskId}")
    public ResponseEntity<TaskStatusDTO> getStatus(@PathVariable UUID taskId) {
        TaskStatusDTO status = taskStatusService.getTaskStatus(taskId);

        if (status == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(status);
    }

    private TaskResponseDTO buildTaskResponseDTO(UUID taskId) {
        return new ValidTaskResponseDTO(
                taskId,
                ResponseStatus.OK,
                "Задача принята в обработку",
                "/api/v1/video/status/" + taskId);
    }

    private String saveFile(MultipartFile file, UUID taskId, String fileType) throws IOException {
        String tempDir = System.getProperty("java.io.tmpdir").replace("\\", "/");
        if (tempDir.endsWith("/")) {
            tempDir = tempDir.substring(0, tempDir.length() - 1);
        }
        tempDir += "/voiceswap/";


        File directory = new File(tempDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String filePath = tempDir + fileType + "_" + taskId + extension;
        file.transferTo(new File(filePath));

        log.info("💾 Файл сохранен: {}", filePath);
        return filePath;
    }

    private ResponseEntity<TaskResponseDTO> buildErrorResponse(UUID taskId, String message, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(new InvalidTaskResponseDTO(taskId, ResponseStatus.ERROR, message));
    }
}
