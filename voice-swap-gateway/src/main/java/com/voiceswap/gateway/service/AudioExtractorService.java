package com.voiceswap.gateway.service;

import com.voiceswap.gateway.exceptions.AudioExtractionException;
import com.voiceswap.gateway.exceptions.UnsupportedVideoFormat;
import com.voiceswap.gateway.model.VideoFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

import java.io.File;

@Service
public class AudioExtractorService {

    private final Logger log = LoggerFactory.getLogger(AudioExtractorService.class);

    @Value("${audio.extractor.format}")
    private String targetFormat;

    @Value("${audio.extractor.sample-rate}")
    private int sampleRate;

    @Value("${audio.extractor.channels}")
    private int channels;

    @Value("${audio.extractor.codec}")
    private String codec;

    @Value("${audio.extractor.bit-rate}")
    private int bitRate;

    public String extractAudio(String videoPath) {
        try {
            log.info("🎬 Extracting audio from: {}", videoPath);

            File videoFile = new File(videoPath);
            File audioFile = buildAudioFile(videoPath);

            AudioAttributes audioAttributes = buildAudioAttributes();

            EncodingAttributes encodingAttributes = buildEncodingAttributes(audioAttributes);

            encodeAudio(videoFile, audioFile, encodingAttributes);

            log.info("✅ Audio extracted to: {}", audioFile.getAbsolutePath());
            return audioFile.getAbsolutePath();
        } catch (UnsupportedVideoFormat exception) {
            log.error("❌ Unsupported format: {}", videoPath, exception);
            throw exception;
        } catch (Exception e) {
            log.error("❌ Failed to extract audio from: {}", videoPath, e);
            throw new AudioExtractionException("Audio extraction failed: " + e.getMessage(), e);
        }
    }

    private void encodeAudio(File videoFile,
                             File audioFile,
                             EncodingAttributes encodingAttributes) throws EncoderException {
        MultimediaObject multimediaObject = new MultimediaObject(videoFile);
        Encoder encoder = new Encoder();
        encoder.encode(multimediaObject, audioFile, encodingAttributes);
    }

    private AudioAttributes buildAudioAttributes() {
        AudioAttributes audioAttributes = new AudioAttributes();
        audioAttributes.setCodec(codec);
        audioAttributes.setBitRate(bitRate);
        audioAttributes.setSamplingRate(sampleRate);
        audioAttributes.setChannels(channels);
        return audioAttributes;
    }

    private EncodingAttributes buildEncodingAttributes(AudioAttributes audioAttributes) {
        EncodingAttributes encodingAttributes = new EncodingAttributes();
        encodingAttributes.setOutputFormat(targetFormat);
        encodingAttributes.setAudioAttributes(audioAttributes);
        return encodingAttributes;
    }

    private File buildAudioFile(String videoPath) {
        if (videoPath == null || videoPath.isBlank()) {
            throw new IllegalArgumentException("Video path mustn't be null or empty");
        }

        VideoFormat videoFormat = VideoFormat.getFormatByPath(videoPath);
        String extension = videoFormat.extension();

        String basePath = videoPath.substring(0, videoPath.length() - extension.length());
        String audioPath = basePath + '.' + targetFormat;

        File audioFile = new File(audioPath);

        if (audioFile.exists()) {
            log.warn("Audio file already exists, will be overwritten: {}", audioPath);
        }

        return audioFile;
    }
}
