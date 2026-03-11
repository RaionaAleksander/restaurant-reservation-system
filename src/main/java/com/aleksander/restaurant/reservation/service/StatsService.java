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

        long count = reservationRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("status"), ReservationStatus.COMPLETED)).stream()
                .filter(r -> !r.getStartTime().isBefore(start) && r.getStartTime().isBefore(end))
                .count();

        return ReservationCountDTO.builder()
                .days(days)
                .reservationsCount(count)
                .build();
    }

    public List<DailyReservationStatsDTO> getDailyReservationsCountLastNDaysStats(int days) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1);

        List<Object[]> counts = reservationRepository.countReservationsGroupedByDate(startDate.atStartOfDay(),
                today.plusDays(1).atStartOfDay());

        Map<LocalDate, Long> countsMap = counts.stream()
                .collect(Collectors.toMap(
                        obj -> ((LocalDateTime) obj[0]).toLocalDate(),
                        obj -> (Long) obj[1]));

        List<DailyReservationStatsDTO> result = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate day = startDate.plusDays(i);
            long count = countsMap.getOrDefault(day, 0L);
            result.add(new DailyReservationStatsDTO(day, count));
        }

        return result;
    }
}