package com.aleksander.restaurant.reservation.dto;

import java.time.LocalDateTime;

import com.aleksander.restaurant.reservation.model.Zone;

import lombok.Data;

@Data
public class TableFilter {

    private Integer capacity;
    private Integer minCapacity;

    private Zone zone;

    private Boolean nearWindow;
    private Boolean nearKidsZone;
    private Boolean quietCorner;
    private Boolean accessible;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
