package com.voiceswap.bank.service;

import com.voiceswap.bank.model.sample.ActorSample;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SampleBankService {
    public ActorSample registration(UUID profileId, MultipartFile sample) {
        return new ActorSample(UUID.randomUUID(), profileId);
    }

    public List<ActorSample> getSamplesByProfileId(UUID profileId) {
        return new ArrayList<>();
    }

    public void deleteSample(UUID profileId, UUID sampleId) {

    }

    public ActorSample update(UUID profileId, MultipartFile sample) {
        return new ActorSample(UUID.randomUUID(), profileId);
    }
}
