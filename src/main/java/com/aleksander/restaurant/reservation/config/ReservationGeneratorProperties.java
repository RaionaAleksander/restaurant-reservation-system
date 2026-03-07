package com.aleksander.restaurant.reservation.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "reservation.generator")
public class ReservationGeneratorProperties {

    private int count;
    private List<String> clientNames;

    private LocalTime openTime;
    private LocalTime closeTime;

    private int daysRange;

    private int minDuration;
    private int maxDuration;
}