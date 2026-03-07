package com.aleksander.restaurant.reservation.config;

import java.time.LocalTime;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "reservation.rules")
public class ReservationRulesProperties {

    private LocalTime openTime;
    private LocalTime closeTime;

    private int daysAhead;

    private Duration minDuration;
    private Duration maxDuration;

}