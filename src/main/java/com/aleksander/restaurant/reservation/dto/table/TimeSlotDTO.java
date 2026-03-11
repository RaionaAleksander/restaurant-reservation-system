package com.aleksander.restaurant.reservation.dto.table;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TimeSlotDTO {

    private LocalDateTime startTime;
    private LocalDateTime endTime;

}