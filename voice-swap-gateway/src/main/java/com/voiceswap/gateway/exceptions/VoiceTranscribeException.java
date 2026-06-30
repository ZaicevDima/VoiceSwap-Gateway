package com.voiceswap.gateway.exceptions;

public class VoiceTranscribeException extends RuntimeException {
    public VoiceTranscribeException(String message, Throwable cause) {
        super(message, cause);
    }
}
