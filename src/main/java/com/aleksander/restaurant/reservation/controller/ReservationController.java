package com.aleksander.restaurant.reservation.controller;

import com.aleksander.restaurant.reservation.model.Reservation;
import com.aleksander.restaurant.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    @Operation(summary = "Get all reservations")
    public List<Reservation> getAllReservations() {
        return reservationService.getAllReservations();
    }

    @PostMapping
    @Operation(summary = "Create reservation", description = """
                Create reservation respecting restaurant rules:
                - Working hours
                - Future date limits
                - Duration limits
                - 15-minute time slots
            """)
    public Reservation createReservation(
            @Parameter(description = "Table ID") @RequestParam Long tableId,

            @Parameter(description = "Customer name") @RequestParam String customerName,

            @Parameter(description = "Start time (format: yyyy-MM-dd'T'HH:mm:ss)") @RequestParam LocalDateTime startTime,

            @Parameter(description = "End time (format: yyyy-MM-dd'T'HH:mm:ss)") @RequestParam LocalDateTime endTime,

            @Parameter(description = "Party size") @RequestParam Integer partySize) {

        return reservationService.createReservation(
                tableId,
                customerName,
                startTime,
                endTime,
                partySize);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id) {

        reservationService.cancelReservation(id);

        return ResponseEntity.noContent().build();
    }
}
