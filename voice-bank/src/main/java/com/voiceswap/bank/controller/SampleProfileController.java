package com.voiceswap.bank.controller;

import com.voiceswap.bank.dto.sample.ActorSampleResponseDTO;
import com.voiceswap.bank.model.sample.ActorSample;
import com.voiceswap.bank.service.SampleBankService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/api/v1/{profileId}/samples")
public class SampleProfileController {

    private final SampleBankService sampleBankService;
    private final Logger log = LoggerFactory.getLogger(SampleProfileController.class);

    public SampleProfileController(SampleBankService sampleBankService) {
        this.sampleBankService = sampleBankService;
    }

    @GetMapping
    public ResponseEntity<List<ActorSampleResponseDTO>> getSamples(@PathVariable UUID profileId) {
        log.info("Getting samples for profile: {}", profileId);
        List<ActorSample> profiles = sampleBankService.getSamplesByProfileId(profileId);
        List<ActorSampleResponseDTO> sampleResponseDTOS = profiles.stream()
                .map(this::mapToActorSampleResponseDTO)
                .toList();

        return ResponseEntity.ok(sampleResponseDTOS);
    }

    @PostMapping("/registration")
    public ResponseEntity<ActorSampleResponseDTO> registrateProfileSample(@PathVariable UUID profileId,
                                                                          @RequestParam("file") MultipartFile sample) {
        log.info("Adding sample to profile: {}", profileId);
        ActorSample actorSample = sampleBankService.registration(profileId, sample);
        ActorSampleResponseDTO sampleResponseDTO = mapToActorSampleResponseDTO(actorSample);

        return ResponseEntity.status(CREATED).body(sampleResponseDTO);
    }

    @PutMapping("/{sampleId}")
    public ResponseEntity<ActorSampleResponseDTO> updateSample(
            @PathVariable UUID profileId,
            @PathVariable UUID sampleId,
            @RequestParam("file") MultipartFile sample) {
        log.info("Updating sample: {} for profile: {}", sampleId, profileId);
        ActorSample actorSample = sampleBankService.update(profileId, sample);
        ActorSampleResponseDTO sampleResponseDTO = mapToActorSampleResponseDTO(actorSample);

        return ResponseEntity.ok(sampleResponseDTO);
    }

    @DeleteMapping("/{sampleId}")
    public ResponseEntity<Void> deleteSample(
            @PathVariable UUID profileId,
            @PathVariable UUID sampleId) {
        log.info("Deleting sample: {} from profile: {}", sampleId, profileId);
        sampleBankService.deleteSample(profileId, sampleId);
        return ResponseEntity.noContent().build();
    }

    private ActorSampleResponseDTO mapToActorSampleResponseDTO(ActorSample actorSample) {
        return new ActorSampleResponseDTO(actorSample.id(), actorSample.profileId());
    }
}
