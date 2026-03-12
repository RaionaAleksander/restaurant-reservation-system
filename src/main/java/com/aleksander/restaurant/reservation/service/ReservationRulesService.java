package com.aleksander.restaurant.reservation.service;

import org.springframework.stereotype.Service;

import com.aleksander.restaurant.reservation.config.ReservationRulesProperties;
import com.aleksander.restaurant.reservation.dto.ReservationRulesDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationRulesService {

    private final ReservationRulesProperties rules;

    public ReservationRulesDTO getRules() {

        return ReservationRulesDTO.builder()
                .openTime(rules.getOpenTime().toString())
                .closeTime(rules.getCloseTime().toString())
                .daysAhead(rules.getDaysAhead())
                .minDurationMinutes(rules.getMinDuration().toMinutes())
                .maxDurationMinutes(rules.getMaxDuration().toMinutes())
                .build();
    }
}