package com.voiceswap.cloner.model;

public record CloneRequest(
        String transcribedText,
        String voiceSamplePath
) {
}
