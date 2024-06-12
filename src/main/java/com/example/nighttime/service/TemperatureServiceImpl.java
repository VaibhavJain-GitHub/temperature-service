package com.example.nighttime.service;

import com.example.nighttime.dto.TemperatureRequestDTO;
import com.example.nighttime.dto.TemperatureResponseDTO;
import com.example.nighttime.dto.SunriseSunsetResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TemperatureServiceImpl implements TemperatureService {

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Value("${webclient.base-url}")
    private String baseUrl;

    private static final int NIGHT_TEMP = 2700;
    private static final int DAY_TEMP = 6000;

    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d{1,2}):(\\d{2}):(\\d{2}) (AM|PM)");

    @Override
    public Mono<TemperatureResponseDTO> getNightTimeTemperature(TemperatureRequestDTO requestDTO) {
        String url = String.format("%s?lat=%f&lng=%f", baseUrl, requestDTO.getLat(), requestDTO.getLng());

        return webClientBuilder.build()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(SunriseSunsetResponse.class)
                .map(response -> {
                    if ("OK".equals(response.getStatus())) {
                        return calculateTemperature(response.getResults());
                    } else {
                        throw new RuntimeException("Failed to get sunrise-sunset data");
                    }
                });
    }

    private TemperatureResponseDTO calculateTemperature(SunriseSunsetResponse.Results results) {
        ZoneId localZone = ZoneId.systemDefault();

        LocalTime sunrise = convertToLocalTime(results.getSunrise(), localZone);
        LocalTime sunset = convertToLocalTime(results.getSunset(), localZone);
        LocalTime civilTwilightBegin = convertToLocalTime(results.getCivil_twilight_begin(), localZone);
        LocalTime civilTwilightEnd = convertToLocalTime(results.getCivil_twilight_end(), localZone);

        Duration durationToSunrise = Duration.between(civilTwilightBegin, sunrise).multipliedBy(2);
        Duration durationToTwilightEnd = Duration.between(sunset, civilTwilightEnd).multipliedBy(2);

        LocalTime extendedTwilightBegin = civilTwilightBegin.plus(durationToSunrise);
        LocalTime extendedTwilightEnd = civilTwilightEnd.minus(durationToTwilightEnd);

        int temperature;
        LocalTime now = LocalTime.now(localZone);
        if (now.isBefore(civilTwilightBegin) || now.isAfter(civilTwilightEnd)) {
            temperature = NIGHT_TEMP; // Night time
        } else if (now.isAfter(civilTwilightBegin) && now.isBefore(extendedTwilightBegin)) {
            temperature = calculateLinearTemperature(civilTwilightBegin, extendedTwilightBegin, NIGHT_TEMP, DAY_TEMP, now);
        } else if (now.isAfter(sunset) && now.isBefore(extendedTwilightEnd)) {
            temperature = calculateLinearTemperature(sunset, extendedTwilightEnd, DAY_TEMP, NIGHT_TEMP, now);
        } else {
            temperature = DAY_TEMP; // Day time
        }

        return new TemperatureResponseDTO(temperature);
    }

    private LocalTime convertToLocalTime(String timeString, ZoneId localZone) {
        Matcher matcher = TIME_PATTERN.matcher(timeString);
        if (matcher.matches()) {
            int hour = Integer.parseInt(matcher.group(1));
            int minute = Integer.parseInt(matcher.group(2));
            int second = Integer.parseInt(matcher.group(3));
            String ampm = matcher.group(4);

            if ("PM".equals(ampm) && hour != 12) {
                hour += 12;
            } else if ("AM".equals(ampm) && hour == 12) {
                hour = 0;
            }

            LocalTime time = LocalTime.of(hour, minute, second);
            ZonedDateTime utcTime = time.atDate(LocalDate.now()).atZone(ZoneId.of("UTC"));
            ZonedDateTime localTime = utcTime.withZoneSameInstant(localZone);

            return localTime.toLocalTime();
        } else {
            throw new IllegalArgumentException("Invalid time format: " + timeString);
        }
    }

    private int calculateLinearTemperature(LocalTime start, LocalTime end, int startTemp, int endTemp, LocalTime currentTime) {
        Duration totalDuration = Duration.between(start, end);
        Duration elapsedDuration = Duration.between(start, currentTime);
        double fraction = (double) elapsedDuration.toMinutes() / totalDuration.toMinutes();
        return (int) (startTemp + fraction * (endTemp - startTemp));
    }
}
