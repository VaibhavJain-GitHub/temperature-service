package com.example.nighttime.controller;

import com.example.nighttime.dto.TemperatureRequestDTO;
import com.example.nighttime.dto.TemperatureResponseDTO;
import com.example.nighttime.service.TemperatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class TemperatureController {

    @Autowired
    private TemperatureService temperatureService;

    @GetMapping("/night-time-temperature")
    public Mono<TemperatureResponseDTO> getNightTimeTemperature(@RequestParam float lat, @RequestParam float lng) {
        TemperatureRequestDTO requestDTO = new TemperatureRequestDTO(lat, lng);
        return temperatureService.getNightTimeTemperature(requestDTO);
    }
}
