package com.voiceswap.gateway.model;

import java.util.Arrays;
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

    public static Set<String> getAllVideoFormats() {
        return Arrays.stream(VideoFormat.values())
                .map(videoFormat -> videoFormat.extension)
                .collect(Collectors.toSet());
    }
}
