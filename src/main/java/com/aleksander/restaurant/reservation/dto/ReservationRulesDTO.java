package com.aleksander.restaurant.reservation.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReservationRulesDTO {

    private String openTime;
    private String closeTime;

    private int daysAhead;

    private long minDurationMinutes;
    private long maxDurationMinutes;
}