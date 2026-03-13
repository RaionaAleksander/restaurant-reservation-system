package com.aleksander.restaurant.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.aleksander.restaurant.reservation.dto.ReservationSlotDTO;
import com.aleksander.restaurant.reservation.dto.ReservationTimelineDTO;
import com.aleksander.restaurant.reservation.dto.TableTimelineDTO;
import com.aleksander.restaurant.reservation.model.Reservation;
import com.aleksander.restaurant.reservation.repository.ReservationRepository;
import com.aleksander.restaurant.reservation.repository.RestaurantTableRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationTimelineService {

    private final ReservationRepository reservationRepository;
    private final RestaurantTableRepository tableRepository;

    public ReservationTimelineDTO getTimeline(LocalDate date) {

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Reservation> reservations = reservationRepository.findByStartTimeBetween(startOfDay, endOfDay);

        Map<Long, List<Reservation>> reservationsByTable = reservations.stream()
                .collect(Collectors.groupingBy(r -> r.getTable().getId()));

        List<TableTimelineDTO> tables = tableRepository.findAll().stream()
                .map(table -> {

                    List<ReservationSlotDTO> slots = reservationsByTable
                            .getOrDefault(table.getId(), List.of())
                            .stream()
                            .map(res -> ReservationSlotDTO.builder()
                                    .startTime(res.getStartTime())
                                    .endTime(res.getEndTime())
                                    .customerName(res.getCustomerName())
                                    .build())
                            .toList();

                    return TableTimelineDTO.builder()
                            .tableId(table.getId())
                            .tableNumber(table.getTableNumber())
                            .reservations(slots)
                            .build();
                })
                .toList();

        return ReservationTimelineDTO.builder()
                .date(date)
                .tables(tables)
                .build();
    }
}