package com.example.nighttime.service;

import com.example.nighttime.dto.TemperatureRequestDTO;
import com.example.nighttime.dto.TemperatureResponseDTO;
import reactor.core.publisher.Mono;

public interface TemperatureService {
    Mono<TemperatureResponseDTO> getNightTimeTemperature(TemperatureRequestDTO requestDTO);
}
