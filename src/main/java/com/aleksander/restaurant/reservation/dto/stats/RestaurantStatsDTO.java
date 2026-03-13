package com.aleksander.restaurant.reservation.dto.stats;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RestaurantStatsDTO {
    private long totalTables;
    private long totalReservations;
    private long activeReservations;
    private long todayReservations;
    private long cancelledReservations;
    private long completedReservations;
}