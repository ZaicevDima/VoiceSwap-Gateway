package com.voiceswap.gateway.service;

import com.voiceswap.gateway.exceptions.VoiceCloneException;
import com.voiceswap.gateway.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

import static com.voiceswap.gateway.model.TaskStatus.*;

@Service
public class OrchestrationService {

    Logger log = LoggerFactory.getLogger(OrchestrationService.class);

    @Value("${services.voice-cloner.url}")
    private String voiceClonerUrl;

    private final TaskStatusService taskStatusService;
    private final AudioExtractorService audioExtractor;
    private final ASRService asrService;
    private final VideoMuxerService videoMuxer;
    private final WebClient webClient;

    public OrchestrationService(TaskStatusService taskStatusService,
                                AudioExtractorService audioExtractor,
                                ASRService asrService,
                                VideoMuxerService videoMuxer) {
        this.taskStatusService = taskStatusService;
        this.audioExtractor = audioExtractor;
        this.asrService = asrService;
        this.videoMuxer = videoMuxer;
        this.webClient = WebClient.builder().build();
    }

    @Async
    public void performVoiceSwap(UUID taskId, String videoPath, String voiceSamplePath) {
        log.info("🎯 Starting voice swap for task: {}", taskId);
        TaskStatusDTO taskStatus;

        try {
            taskStatus = new ValidTaskStatusDTO(taskId, PROCESSING, "0%", null);
            log.info("⏳ Extracting audio for task: {}", taskId);
            taskStatusService.updateStatus(taskStatus);

            // Извлекаем аудио из видео
            String originalAudioPath = audioExtractor.extractAudio(videoPath);
            taskStatus = new ValidTaskStatusDTO(taskId, PROCESSING, "25%", null);
            taskStatusService.updateStatus(taskStatus);
            log.info("✅ Audio extracted for task: {}", taskId);

            // Распознаем текст из аудио
            String transcribedText = asrService.transcribe(originalAudioPath);
            taskStatus = new ValidTaskStatusDTO(taskId, PROCESSING, "50%", null);
            taskStatusService.updateStatus(taskStatus);
            log.info("✅ Audio transcribe for task: {}", taskId);

            // Клонируем голос и синтезируем речь
            String newAudioPath = cloneVoice(transcribedText, voiceSamplePath); //voiceCloner.cloneVoice(transcribedText, voiceSamplePath);
            taskStatus = new ValidTaskStatusDTO(taskId, PROCESSING, "75%", null);
            taskStatusService.updateStatus(taskStatus);
            log.info("✅ Voice clone for task: {}", taskId);


            // Склеиваем видео с новым аудио
            String finalVideoPath = videoMuxer.mux(videoPath, newAudioPath);
            taskStatus = new ValidTaskStatusDTO(taskId, PROCESSING, "90%", null);
            taskStatusService.updateStatus(taskStatus);
            log.info("✅ Video mux with audio for task: {}", taskId);

            // Завершаем
            taskStatus = new ValidTaskStatusDTO(taskId, COMPLETED, "100%", finalVideoPath);
            taskStatusService.updateStatus(taskStatus);

            log.info("✅ Audio extracted for task: {}", taskId);

        } catch (Exception exception) {
            log.error("❌ Voice swap failed for task: {}", taskId, exception);
            taskStatus = new InvalidTaskStatusDTO(taskId, exception.getMessage());
            taskStatusService.updateStatus(taskStatus);
        }
    }

    private String cloneVoice(String transcribedText, String voiceSamplePath) {
        CloneRequest request = new CloneRequest(transcribedText, voiceSamplePath);
        CloneResponse response = webClient.post()
                .uri(voiceClonerUrl + "/api/v1/clone")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(CloneResponse.class)
                .block();

        if (response == null || !"SUCCESS".equals(response.status())) {
            String errorMsg = response != null ? response.errorMessage() : "No response from voice cloner service";
            throw new VoiceCloneException("Voice cloning failed: " + errorMsg);
        }

        return response.outputPath();
    }
}
