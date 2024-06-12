package com.example.nighttime.service;

import com.example.nighttime.dto.TemperatureResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.http.MediaType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class TemperatureServiceIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void testGetNightTimeTemperature() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/night-time-temperature")
                        .queryParam("lat", 36.7201600)
                        .queryParam("lng", -4.4203400)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TemperatureResponseDTO.class)
                .value(response -> {
                    int expectedTemperature = 3400; // Assume test runs at night for validation
                    assert response.getTemperature() == expectedTemperature;
                });
    }
}
