package com.aleksander.restaurant.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.aleksander.restaurant.reservation.dto.stats.DailyReservationStatsDTO;
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
        long completedReservations = reservationRepository.countByStatus(ReservationStatus.COMPLETED);

        LocalDate today = LocalDate.now();
        long todayReservations = reservationRepository.countByStartTimeBetween(
                today.atStartOfDay(),
                today.plusDays(1).atStartOfDay());

        return RestaurantStatsDTO.builder()
                .totalTables(totalTables)
                .totalReservations(totalReservations)
                .activeReservations(activeReservations)
                .completedReservations(completedReservations)
                .cancelledReservations(cancelledReservations)
                .todayReservations(todayReservations)
                .build();
    }

    public ReservationCountDTO getReservationsCountLastNDays(int days) {
        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = LocalDate.now().plusDays(1).atStartOfDay();

        long count = reservationRepository.countByStatusAndStartTimeBetween(
                ReservationStatus.COMPLETED,
                start,
                end);

        return ReservationCountDTO.builder()
                .days(days)
                .reservationsCount(count)
                .build();
    }

    public List<DailyReservationStatsDTO> getDailyReservationsCountLastNDaysStats(int days) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1);

        List<Object[]> counts = reservationRepository.countReservationsGroupedByDateAndStatus(
                ReservationStatus.COMPLETED,
                startDate.atStartOfDay(),
                today.plusDays(1).atStartOfDay());

        Map<LocalDate, Long> countsMap = counts.stream()
                .collect(Collectors.groupingBy(
                        obj -> ((LocalDateTime) obj[0]).toLocalDate(),
                        Collectors.summingLong(obj -> ((Long) obj[1]).longValue())));

        List<DailyReservationStatsDTO> result = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate day = startDate.plusDays(i);
            long count = countsMap.getOrDefault(day, 0L);
            result.add(new DailyReservationStatsDTO(day, count));
        }

        return result;
    }
}