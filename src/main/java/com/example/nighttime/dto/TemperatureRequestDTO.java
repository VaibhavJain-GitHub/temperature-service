package com.example.nighttime.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemperatureRequestDTO {
    private float lat;
    private float lng;
}
