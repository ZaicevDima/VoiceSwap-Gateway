package com.voiceswap.gateway.model;

public sealed interface TaskResponseDTO permits ValidTaskResponseDTO, InvalidTaskResponseDTO {}
