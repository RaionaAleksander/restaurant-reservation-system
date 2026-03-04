package com.aleksander.restaurant.reservation.controller;

import com.aleksander.restaurant.reservation.model.Reservation;
import com.aleksander.restaurant.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public Reservation createReservation(@RequestParam Long tableId,
            @RequestParam String customerName,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime,
            @RequestParam Integer partySize) {

        return reservationService.createReservation(
                tableId,
                customerName,
                startTime,
                endTime,
                partySize);
    }
}
