package com.aleksander.restaurant.reservation.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "reservation.generator")
public class ReservationGeneratorProperties {
    private int count;
    private int countCompleted;
    private int pastDays;
}