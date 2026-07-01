package com.voiceswap.cloner.model;

public record CloneResponse(
        CloneResponseStatus status,
        String outputPath,
        String error // TODO по возможности убрать
) {
}
