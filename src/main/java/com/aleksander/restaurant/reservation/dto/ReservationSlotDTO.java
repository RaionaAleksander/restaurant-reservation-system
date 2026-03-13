package com.aleksander.restaurant.reservation.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReservationSlotDTO {

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String customerName;

}