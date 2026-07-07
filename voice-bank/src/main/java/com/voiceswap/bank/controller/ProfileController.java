package com.voiceswap.bank.controller;

import com.voiceswap.bank.dto.profile.ActorProfileStatusResponseDTO;
import com.voiceswap.bank.dto.profile.VoiceActorProfileRequestDTO;
import com.voiceswap.bank.dto.profile.VoiceActorProfileResponseDTO;
import com.voiceswap.bank.model.profile.VoiceActorProfile;
import com.voiceswap.bank.model.profile.VoiceActorStatus;
import com.voiceswap.bank.service.VoiceProfileBankService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/api/v1/profiles")
public class ProfileController {

    private final VoiceProfileBankService voiceProfileBankService;
    private final Logger log = LoggerFactory.getLogger(ProfileController.class);

    public ProfileController(VoiceProfileBankService voiceProfileBankService) {
        this.voiceProfileBankService = voiceProfileBankService;
    }

    @PostMapping("/registration")
    public ResponseEntity<VoiceActorProfileResponseDTO> registrateProfile(@RequestBody VoiceActorProfileRequestDTO profileDTO) {
        String actorName = getName(profileDTO);

        log.info("Creating voice actor profile: %s".formatted(actorName));
        VoiceActorProfile profile = voiceProfileBankService.registration(actorName);
        VoiceActorProfileResponseDTO profileResponseDTO = mapToVoiceActorProfileResponseDTO(profile);

        return ResponseEntity.status(CREATED).body(profileResponseDTO);
    }

    @GetMapping
    public ResponseEntity<List<VoiceActorProfileResponseDTO>> getAllProfile() {
        log.info("Getting all voice actor profiles");
        List<VoiceActorProfile> profiles = voiceProfileBankService.getAllProfiles();
        List<VoiceActorProfileResponseDTO> voiceActorProfileResponseDTOS = profiles.stream()
                .map(this::mapToVoiceActorProfileResponseDTO)
                .toList();

        return ResponseEntity.ok().body(voiceActorProfileResponseDTOS);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VoiceActorProfileResponseDTO> getProfileById(@PathVariable UUID id) {
        log.info("Getting voice actor profiles by id: %s".formatted(id));
        VoiceActorProfile profile = voiceProfileBankService.getProfileById(id);
        VoiceActorProfileResponseDTO voiceActorProfileResponseDTO = mapToVoiceActorProfileResponseDTO(profile);

        return ResponseEntity.ok().body(voiceActorProfileResponseDTO);
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<ActorProfileStatusResponseDTO> getProfileStatus(@PathVariable UUID id) {
        log.info("Getting profile status: {}", id);
        VoiceActorStatus profileStatus = voiceProfileBankService.getProfileStatus(id);
        ActorProfileStatusResponseDTO statusResponseDTO = mapToActorProfileStatusResponseDTO(profileStatus);

        return ResponseEntity.ok(statusResponseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VoiceActorProfileResponseDTO> updateProfile(
            @PathVariable UUID id,
            @RequestBody String name) {
        log.info("Updating profile with id: {}, name: {}", id, name);
        VoiceActorProfile voiceActorProfile = voiceProfileBankService.update(id, name);
        VoiceActorProfileResponseDTO voiceActorProfileResponseDTO = mapToVoiceActorProfileResponseDTO(voiceActorProfile);

        return ResponseEntity.ok(voiceActorProfileResponseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfile(@PathVariable UUID id) {
        log.info("Deleting profile: {}", id);
        voiceProfileBankService.deleteProfile(id);
        return ResponseEntity.noContent().build();
    }

    private VoiceActorProfileResponseDTO mapToVoiceActorProfileResponseDTO(VoiceActorProfile profile) {
        return new VoiceActorProfileResponseDTO(profile.id(), profile.name());
    }

    private static String getName(VoiceActorProfileRequestDTO profileDTO) {
        return profileDTO.name();
    }

    private ActorProfileStatusResponseDTO mapToActorProfileStatusResponseDTO(VoiceActorStatus profileStatus) {
        String status = profileStatus.status();
        return new ActorProfileStatusResponseDTO(status);
    }
}
