package com.aleksander.restaurant.reservation.dto;

import java.time.LocalDate;

import com.aleksander.restaurant.reservation.model.ReservationStatus;

import lombok.Data;

@Data
public class ReservationFilter {

    private ReservationStatus status;

    private LocalDate date;

    private String customerName;

    private Integer tableNumber;

}