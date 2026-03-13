package com.aleksander.restaurant.reservation.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReservationTimelineDTO {

    private LocalDate date;

    private List<TableTimelineDTO> tables;

}