package com.aleksander.restaurant.reservation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aleksander.restaurant.reservation.dto.ReservationRulesDTO;
import com.aleksander.restaurant.reservation.service.ReservationRulesService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ReservationRulesController {

    private final ReservationRulesService rulesService;

    @GetMapping("/api/reservation-rules")
    public ReservationRulesDTO getRules() {
        return rulesService.getRules();
    }
}