package com.aleksander.restaurant.reservation.dto.stats;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DailyReservationStatsDTO {
    private LocalDate date;
    private long reservationsCount;
}