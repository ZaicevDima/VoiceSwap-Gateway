package com.voiceswap.gateway.service;

import com.voiceswap.gateway.model.InvalidTaskStatusDTO;
import com.voiceswap.gateway.model.TaskStatusDTO;
import com.voiceswap.gateway.model.ValidTaskStatusDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.voiceswap.gateway.model.TaskStatus.*;

@Service
public class OrchestrationService {

    Logger log = LoggerFactory.getLogger(OrchestrationService.class);

    private final TaskStatusService taskStatusService;
    private final AudioExtractorService audioExtractor;
    private final ASRService asrService;                  // TODO
    private final VoiceClonerService voiceCloner;         // TODO
    private final VideoMuxerService videoMuxer;           // TODO

    public OrchestrationService(TaskStatusService taskStatusService,
                                AudioExtractorService audioExtractor,
                                ASRService asrService,
                                VoiceClonerService voiceCloner,
                                VideoMuxerService videoMuxer) {
        this.taskStatusService = taskStatusService;
        this.audioExtractor = audioExtractor;
        this.asrService = asrService;
        this.voiceCloner = voiceCloner;
        this.videoMuxer = videoMuxer;
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
            String newAudioPath = voiceCloner.cloneVoice(transcribedText, voiceSamplePath);
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
}
