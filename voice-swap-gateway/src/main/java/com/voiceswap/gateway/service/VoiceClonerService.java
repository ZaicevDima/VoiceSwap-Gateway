package com.voiceswap.gateway.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceswap.gateway.exceptions.VoiceCloneException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class VoiceClonerService {

    private static final Logger log = LoggerFactory.getLogger(VoiceClonerService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${voice-cloner.python-path:python3}")
    private String pythonPath;

    @Value("${voice-cloner.script-path:scripts/clone_voice.py}")
    private String scriptPath;

    public String cloneVoice(String transcribedText, String voiceSamplePath) {
        if (transcribedText == null || transcribedText.isBlank()) {
            throw new IllegalArgumentException("Text cannot be null or empty");
        }

        File voiceSample = new File(voiceSamplePath);
        if (!voiceSample.exists()) {
            throw new IllegalArgumentException("Voice sample does not exist: " + voiceSamplePath);
        }

        Path tempAudio = null;
        try {
            tempAudio = Files.createTempFile("cloned_", ".wav");
            String outputPath = tempAudio.toString();

            log.info("🎤 Cloning voice for text: '{}'", transcribedText);

            Process process = buildCloningPythonProcess(transcribedText, voiceSamplePath, outputPath);
            StringBuilder processOutput = getPythonProcessOutput(process);

            return parsePythonOutput(processOutput, outputPath);

        } catch (Exception exception) {
            if (tempAudio != null) {
                try {
                    Files.deleteIfExists(tempAudio);
                } catch (IOException e) {
                    log.warn("Could not delete temp file: {}", tempAudio, e);
                }
            }
            log.error("❌ Voice cloning failed: {}", exception.getMessage());
            throw new VoiceCloneException("Voice cloning failed: " + exception.getMessage(), exception);
        }
    }

    private String parsePythonOutput(StringBuilder output, String outputPath) {
        try {
            JsonNode result = objectMapper.readTree(output.toString());
            if ("success".equals(result.path("status").asText())) {
                log.info("✅ Voice cloned to: {}", outputPath);
                return outputPath;
            } else {
                throw new VoiceCloneException("Voice cloning failed: " + result.path("message").asText());
            }
        } catch (Exception e) {
            log.warn("Could not parse Python output: {}", output);

            File audioFile = new File(outputPath);
            if (audioFile.exists() && audioFile.length() > 0) {
                return outputPath;
            }
            throw new VoiceCloneException("Voice cloning failed: " + output);
        }
    }

    private StringBuilder getPythonProcessOutput(Process process) throws IOException, InterruptedException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
                log.debug("Python: {}", line);
            }
        }

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            log.error("Python script failed with code: {}", exitCode);
            throw new VoiceCloneException("Voice cloning failed: " + output);
        }
        return output;
    }

    private Process buildCloningPythonProcess(String transcribedText,
                                              String voiceSamplePath,
                                              String outputPath) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(
                pythonPath,
                scriptPath,
                "--text", transcribedText,
                "--voice-sample", voiceSamplePath,
                "--output", outputPath,
                "--speaker", "ru_speaker_1",
                "--skip-license-check"
        );

        pb.redirectErrorStream(true);
        return pb.start();
    }
}
