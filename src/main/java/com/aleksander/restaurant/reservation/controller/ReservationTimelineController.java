package com.aleksander.restaurant.reservation.controller;

import java.time.LocalDate;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aleksander.restaurant.reservation.dto.ReservationTimelineDTO;
import com.aleksander.restaurant.reservation.service.ReservationTimelineService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationTimelineController {

    private final ReservationTimelineService timelineService;

    @GetMapping("/timeline")
    public ReservationTimelineDTO getTimeline(
            @RequestParam LocalDate date) {
        return timelineService.getTimeline(date);
    }

}