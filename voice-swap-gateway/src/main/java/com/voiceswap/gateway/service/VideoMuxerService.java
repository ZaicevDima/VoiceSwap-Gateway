package com.voiceswap.gateway.service;

import com.voiceswap.gateway.exceptions.VideoMuxerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class VideoMuxerService {

    private final Logger log = LoggerFactory.getLogger(VideoMuxerService.class);

    @Value("${video.muxer.format}")
    private String outputFormat;

    @Value("${video.muxer.ffmpeg-path}")
    private String ffmpegPath;

    @Value("${video.muxer.video-codec}")
    private String videoCodec;

    @Value("${video.muxer.audio-codec}")
    private String audioCodec;

    public String mux(String videoPath, String audioPath) {
        log.info("🎬 Muxing video: {} with audio: {}", videoPath, audioPath);

        validateFile(videoPath, "Video file does not exist: ");
        validateFile(audioPath, "Audio file does not exist: ");

        try {
            String outputPath = buildOutputPath(videoPath);
            ProcessBuilder processBuilder = getProcessBuilder(videoPath, audioPath, outputPath);
            mux(processBuilder);

            log.info("✅ Video muxed to: {}", outputPath);
            return outputPath;
        } catch (Exception e) {
            log.error("❌ Failed to mux video: {}", e.getMessage());
            throw new VideoMuxerException("Video muxing failed: " + e.getMessage(), e);
        }
    }

    private void validateFile(String videoPath, String message) {
        File videoFile = new File(videoPath);

        if (!videoFile.exists()) {
            throw new IllegalArgumentException(message + videoPath);
        }
    }

    private void mux2(ProcessBuilder processBuilder) throws IOException, InterruptedException {
        logCommand(processBuilder);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new VideoMuxerException("FFmpeg failed with code: " + exitCode);
        }
    }
    private void mux(ProcessBuilder processBuilder) throws IOException, InterruptedException {
        logCommand(processBuilder);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        // Читаем вывод в отдельном потоке (чтобы буфер не заполнился)
        Thread outputReader = new Thread(() -> {
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("FFmpeg: {}", line);
                }
            } catch (IOException e) {
                log.warn("Error reading FFmpeg output: {}", e.getMessage());
            }
        });
        outputReader.setDaemon(true);
        outputReader.start();

        int exitCode = process.waitFor();
        outputReader.join(5000); // Ждем 5 секунд для завершения чтения

        if (exitCode != 0) {
            throw new VideoMuxerException("FFmpeg failed with code: " + exitCode);
        }
    }

    private ProcessBuilder getProcessBuilder(String videoPath, String audioPath, String outputPath) {
        if (videoPath == null || videoPath.isBlank()) {
            throw new IllegalArgumentException("Video path cannot be null or empty");
        }
        if (audioPath == null || audioPath.isBlank()) {
            throw new IllegalArgumentException("Audio path cannot be null or empty");
        }

        String normalizedVideo = videoPath.replace("\\", "/");
        String normalizedAudio = audioPath.replace("\\", "/");
        String normalizedOutput = outputPath.replace("\\", "/");

        return new ProcessBuilder(
                ffmpegPath,                    // 1. Программа
                "-i", normalizedVideo,             // 2-3. Входной видеофайл
                "-i", normalizedAudio,             // 4-5. Входной аудиофайл
                "-c:v", videoCodec,              // 6-7. Копировать видео (без перекодирования)
                "-c:a", audioCodec,               // 8-9. Аудио кодек AAC
                "-map", "0:v:0",             // 10-11. Брать видео из первого файла (поток 0)
                "-map", "1:a:0",             // 12-13. Брать аудио из второго файла (поток 0)
                "-shortest",                 // 14. Обрезать по самому короткому файлу
                "-y",                        // 15. Перезаписывать без подтверждения
                "-nostdin",
                normalizedOutput                   // 16. Выходной файл
        );
    }

    private void logCommand(ProcessBuilder pb) {
        log.info("Running command: {}", String.join(" ", pb.command()));
    }

    private String buildOutputPath(String videoPath) {
        String normalizedPath = videoPath.replace("\\", "/");

        int dotIndex = videoPath.lastIndexOf('.');
        if (dotIndex > 0) {
            return videoPath.substring(0, dotIndex) + "_output." + outputFormat;
        }
        return videoPath + "_output." + outputFormat;
    }
}
