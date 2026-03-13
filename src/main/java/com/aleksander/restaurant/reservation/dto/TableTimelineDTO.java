package com.aleksander.restaurant.reservation.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TableTimelineDTO {

    private Long tableId;

    private Integer tableNumber;

    private List<ReservationSlotDTO> reservations;

}