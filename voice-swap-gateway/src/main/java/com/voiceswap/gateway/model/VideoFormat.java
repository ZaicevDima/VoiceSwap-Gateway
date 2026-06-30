package com.voiceswap.gateway.model;

import com.voiceswap.gateway.exceptions.UnsupportedVideoFormat;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public enum VideoFormat {
    MP4(".mp4"),
    AVI(".avi"),
    MOV(".mov"),
    MKV(".mkv"),
    FLV(".flv"),
    WEBM(".webm"),
    MPEG(".mpeg");

    private final String extension;

    VideoFormat(String extension) {
        this.extension = extension;
    }

    public String extension() {
        return extension;
    }

    public static Set<String> getAllVideoFormats() {
        return Arrays.stream(VideoFormat.values())
                .map(videoFormat -> videoFormat.extension)
                .collect(Collectors.toSet());
    }

    public static VideoFormat getFormatByPath(String path) {
        String lowerPath = path.toLowerCase();

        return Arrays.stream(VideoFormat.values())
                .filter(format -> lowerPath.endsWith(format.extension))
                .findFirst()
                .orElseThrow(() -> new UnsupportedVideoFormat(
                        String.format("Unsupported video format: '%s'. Supported: %s",
                                path, VideoFormat.getAllVideoFormats())
                ));
    }
}
