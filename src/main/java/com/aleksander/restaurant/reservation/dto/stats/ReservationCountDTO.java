package com.aleksander.restaurant.reservation.dto.stats;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReservationCountDTO {
    private int days;
    private long reservationsCount;
}