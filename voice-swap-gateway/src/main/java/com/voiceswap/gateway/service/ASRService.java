package com.voiceswap.gateway.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceswap.gateway.exceptions.VoiceTranscribeException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;

import java.io.*;

@Service
public class ASRService {

    private final Logger log = LoggerFactory.getLogger(ASRService.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Model model;

    @Value("${asr.model-path}")
    private String modelPath;

    @Value("${asr.sample-rate:16000}")
    private int sampleRate;

    private static final LogLevel VOSK_LOG_LEVEL = LogLevel.WARNINGS;
    private static final int BUFFER_SIZE = 4096;

    @PostConstruct
    public void init() {
        try {
            LibVosk.setLogLevel(VOSK_LOG_LEVEL);

            log.info("Загрузка модели ASR из: {}", modelPath);
            model = new Model(modelPath);
            log.info("✅ Модель ASR успешно загружена");
        } catch (Exception e) {
            log.error("❌ Ошибка загрузки модели ASR: {}", e.getMessage());
            throw new RuntimeException("Не удалось загрузить модель ASR", e);
        }
    }

    public String transcribe(String audioPath) {
        validateFile(audioPath);

        try (InputStream audioStream = new FileInputStream(audioPath);
             Recognizer recognizer = new Recognizer(model, sampleRate)) {
            log.info("Начинаем распознавание: {}", audioPath);

            recognize(audioStream, recognizer);
            String finalResult = recognizer.getFinalResult();
            log.info("✅ Распознавание завершено: {}", finalResult);

            String transcribedText = extractTextFromResult(finalResult);
            log.info("Распознанный текст: {}", transcribedText);

            return transcribedText;
        } catch (Exception e) {
            log.error("❌ Ошибка распознавания речи: {}", e.getMessage());
            throw new VoiceTranscribeException("Ошибка распознавания речи", e);
        }
    }

    private void validateFile(String audioPath) {
        if (audioPath == null || audioPath.isBlank()) {
            throw new IllegalArgumentException("Audio path cannot be null or empty");
        }

        File audioFile = new File(audioPath);
        if (!audioFile.exists()) {
            throw new IllegalArgumentException("Audio file not found: " + audioPath);
        }
    }

    private void recognize(InputStream audioStream, Recognizer recognizer) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;

        while ((bytesRead = audioStream.read(buffer)) >= 0) {
            if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                String result = recognizer.getResult();
                log.debug("Промежуточный результат: {}", result);
            }
        }
    }

    private String extractTextFromResult(String jsonResult) {
        try {
            JsonNode node = objectMapper.readTree(jsonResult);
            String text = node.path("text").asText();
            if (text.isEmpty()) {
                log.warn("Распознавание не дало результата для: {}", jsonResult);
            }
            return text;
        } catch (JsonProcessingException e) {
            log.warn("Не удалось распарсить результат Vosk: {}", jsonResult);
            return jsonResult;
        }
    }

    @PreDestroy
    public void cleanup() {
        if (model != null) {
            model.close();
            log.info("Модель ASR освобождена");
        }
    }
}
