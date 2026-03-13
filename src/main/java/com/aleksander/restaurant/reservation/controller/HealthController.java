package com.aleksander.restaurant.reservation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aleksander.restaurant.reservation.dto.HealthResponse;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public HealthResponse health() {

        return HealthResponse.builder()
                .status("UP")
                .build();
    }
}
