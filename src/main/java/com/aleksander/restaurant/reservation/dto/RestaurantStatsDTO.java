package com.aleksander.restaurant.reservation.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RestaurantStatsDTO {

    private long tables;
    private long reservations;
}