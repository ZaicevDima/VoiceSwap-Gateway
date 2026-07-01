package com.voiceswap.cloner.exceptions;

public class VoiceCloneException extends RuntimeException {

    public VoiceCloneException(String message) {
        super(message);
    }

    public VoiceCloneException(String message, Throwable cause) {
        super(message, cause);
    }
}
