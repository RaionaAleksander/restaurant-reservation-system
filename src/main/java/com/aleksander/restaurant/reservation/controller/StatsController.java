package com.aleksander.restaurant.reservation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aleksander.restaurant.reservation.dto.RestaurantStatsDTO;
import com.aleksander.restaurant.reservation.service.StatsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping
    public RestaurantStatsDTO getStats() {
        return statsService.getStats();
    }
}