package com.voiceswap.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class VoiceBankApplication {
    public static void main(String[] args) {
        SpringApplication.run(VoiceBankApplication.class);
    }
}