package com.voiceswap.cloner.exceptions;

public class VideoMuxerException extends RuntimeException {
    public VideoMuxerException(String message) {
        super(message);
    }

    public VideoMuxerException(String message, Throwable cause) {
        super(message, cause);
    }
}
