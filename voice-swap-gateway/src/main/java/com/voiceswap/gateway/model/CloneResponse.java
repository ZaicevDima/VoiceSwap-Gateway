package com.voiceswap.gateway.model;

public record CloneResponse(String status,
                            String outputPath,
                            String errorMessage) {
}
