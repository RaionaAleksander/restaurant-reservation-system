package com.aleksander.restaurant.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.aleksander.restaurant.reservation.dto.stats.ReservationCountDTO;
import com.aleksander.restaurant.reservation.dto.stats.RestaurantStatsDTO;
import com.aleksander.restaurant.reservation.model.ReservationStatus;
import com.aleksander.restaurant.reservation.repository.ReservationRepository;
import com.aleksander.restaurant.reservation.repository.RestaurantTableRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final RestaurantTableRepository tableRepository;
    private final ReservationRepository reservationRepository;

    public RestaurantStatsDTO getDashboardStats() {
        long totalTables = tableRepository.count();
        long totalReservations = reservationRepository.count();
        long activeReservations = reservationRepository.countByStatus(ReservationStatus.ACTIVE);
        long cancelledReservations = reservationRepository.countByStatus(ReservationStatus.CANCELLED);

        LocalDate today = LocalDate.now();
        long todayReservations = reservationRepository.countByStartTimeBetween(
                today.atStartOfDay(),
                today.plusDays(1).atStartOfDay());

        return RestaurantStatsDTO.builder()
                .totalTables(totalTables)
                .totalReservations(totalReservations)
                .activeReservations(activeReservations)
                .cancelledReservations(cancelledReservations)
                .todayReservations(todayReservations)
                .build();
    }

    public ReservationCountDTO getReservationsCountLastNDays(int days) {
        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = LocalDate.now().plusDays(1).atStartOfDay();

        long count = reservationRepository.countByStartTimeBetween(start, end);

        return ReservationCountDTO.builder()
                .days(days)
                .reservationsCount(count)
                .build();
    }
}