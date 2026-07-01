package com.voiceswap.cloner.controller;



import com.voiceswap.cloner.model.CloneRequest;
import com.voiceswap.cloner.model.CloneResponse;
import com.voiceswap.cloner.model.CloneResponseStatus;
import com.voiceswap.cloner.service.VoiceClonerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

import static com.voiceswap.cloner.model.CloneResponseStatus.FAILED;
import static com.voiceswap.cloner.model.CloneResponseStatus.SUCCESS;

@RestController
@RequestMapping("api/v1/clone")
public class VoiceClonerController {

    private static final Logger log = LoggerFactory.getLogger(VoiceClonerController.class);

    private final VoiceClonerService voiceClonerService;

    public VoiceClonerController(VoiceClonerService voiceClonerService) {
        this.voiceClonerService = voiceClonerService;
    }

    @PostMapping
    public CloneResponse cloneVoice(@RequestBody CloneRequest request) {
        Objects.requireNonNull(request, "request mustn't be null");
        log.info("endpoint clone voice catch request");
        try {
            String originalText = request.transcribedText();
            String voiceSamplePath = request.voiceSamplePath();

            String outputPath = voiceClonerService.cloneVoice2(originalText, voiceSamplePath);
            log.info("endpoint clone voice successfully finished");

            return new CloneResponse(SUCCESS, outputPath, null);
        } catch (Exception exception) {
            log.error("endpoint clone voice failed: {}", exception.getMessage(), exception);
            return new CloneResponse(FAILED, null, exception.getMessage());
        }
    }
}
