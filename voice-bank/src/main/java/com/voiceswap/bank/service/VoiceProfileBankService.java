package com.voiceswap.bank.service;

import com.voiceswap.bank.model.profile.VoiceActorProfile;
import com.voiceswap.bank.model.profile.VoiceActorStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class VoiceProfileBankService {

    public VoiceActorProfile registration(String actorName) {
        return new VoiceActorProfile(UUID.randomUUID(), actorName);
    }

    public VoiceActorStatus getProfileStatus(UUID id) {
        return new VoiceActorStatus("status");
    }

    public List<VoiceActorProfile> getAllProfiles() {
        return new ArrayList<>();
    }

    public VoiceActorProfile getProfileById(UUID id) {
        return new VoiceActorProfile(id, "temp name");
    }

    public VoiceActorProfile update(UUID id, String name) {
        return new VoiceActorProfile(id, name);
    }

    public void deleteProfile(UUID id) {
    }
}
