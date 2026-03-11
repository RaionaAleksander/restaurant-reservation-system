package com.aleksander.restaurant.reservation.service;

import org.springframework.stereotype.Service;

import com.aleksander.restaurant.reservation.dto.RestaurantStatsDTO;
import com.aleksander.restaurant.reservation.repository.ReservationRepository;
import com.aleksander.restaurant.reservation.repository.RestaurantTableRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final RestaurantTableRepository tableRepository;
    private final ReservationRepository reservationRepository;

    public RestaurantStatsDTO getStats() {

        long tableCount = tableRepository.count();
        long reservationCount = reservationRepository.count();

        return RestaurantStatsDTO.builder()
                .tables(tableCount)
                .reservations(reservationCount)
                .build();
    }
}