package com.aleksander.restaurant.reservation.dto;

import lombok.Builder;
import lombok.Data;

import com.aleksander.restaurant.reservation.model.ReservationStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class ReservationDTO {

    private Long id;

    private Long tableId;
    private Integer tableNumber;

    private String customerName;
    private Integer partySize;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private ReservationStatus status;
}
