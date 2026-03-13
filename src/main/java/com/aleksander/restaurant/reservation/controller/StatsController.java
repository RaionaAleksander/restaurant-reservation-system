package com.aleksander.restaurant.reservation.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aleksander.restaurant.reservation.dto.stats.DailyReservationStatsDTO;
import com.aleksander.restaurant.reservation.dto.stats.ReservationCountDTO;
import com.aleksander.restaurant.reservation.dto.stats.RestaurantStatsDTO;
import com.aleksander.restaurant.reservation.service.StatsService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping
    @Operation(summary = "Dashboard stats", description = "Returns general restaurant statistics: total tables, total reservations, active reservations, cancelled reservations, today's reservations.")
    public RestaurantStatsDTO getDashboardStats() {
        return statsService.getDashboardStats();
    }

    @GetMapping("/reservations")
    @Operation(summary = "Reservations count for last N days", description = "Returns total reservations in the last specified number of days.")
    public ReservationCountDTO getReservationsCountLastNDays(
            @RequestParam(defaultValue = "7") int days) {
        return statsService.getReservationsCountLastNDays(days);
    }

    @GetMapping("/reservations/daily")
    @Operation(summary = "Get daily reservations count for the last N days")
    public List<DailyReservationStatsDTO> getDailyReservations(
            @RequestParam int days) {
        return statsService.getDailyReservationsCountLastNDaysStats(days);
    }
}