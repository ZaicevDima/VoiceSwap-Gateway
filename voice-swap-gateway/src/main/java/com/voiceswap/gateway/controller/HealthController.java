package com.voiceswap.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "status","OK",
                "service", "VoiceSwap Gateway",
                "version", "1.0.0");
    }
}
