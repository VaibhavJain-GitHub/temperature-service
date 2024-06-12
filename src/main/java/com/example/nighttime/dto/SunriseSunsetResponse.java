package com.example.nighttime.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SunriseSunsetResponse {
    private Results results;
    private String status;

    @Data
    @NoArgsConstructor
    public static class Results {
        private String sunrise;
        private String sunset;
        private String civil_twilight_begin;
        private String civil_twilight_end;
    }
}
