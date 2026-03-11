package com.aleksander.restaurant.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.aleksander.restaurant.reservation.config.ReservationRulesProperties;
import com.aleksander.restaurant.reservation.dto.TableDTO;
import com.aleksander.restaurant.reservation.dto.TableFilter;
import com.aleksander.restaurant.reservation.dto.table.TimeSlotDTO;
import com.aleksander.restaurant.reservation.model.Reservation;
import com.aleksander.restaurant.reservation.model.ReservationStatus;
import com.aleksander.restaurant.reservation.model.RestaurantTable;
import com.aleksander.restaurant.reservation.repository.ReservationRepository;
import com.aleksander.restaurant.reservation.repository.RestaurantTableRepository;
import com.aleksander.restaurant.reservation.specification.TableSpecification;
import static com.aleksander.restaurant.reservation.util.ReservationTimeUtils.overlaps;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TableService {

    private final RestaurantTableRepository tableRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationRulesProperties rulesProperties;

    public List<TableDTO> findTables(TableFilter filter) {

        Specification<RestaurantTable> spec = Specification.unrestricted();

        spec = spec.and(TableSpecification.hasCapacity(filter.getCapacity()));
        spec = spec.and(TableSpecification.hasZone(filter.getZone()));
        spec = spec.and(TableSpecification.nearWindow(filter.getNearWindow()));
        spec = spec.and(TableSpecification.nearKidsZone(filter.getNearKidsZone()));
        spec = spec.and(TableSpecification.quietCorner(filter.getQuietCorner()));
        spec = spec.and(TableSpecification.accessible(filter.getAccessible()));

        List<RestaurantTable> tables = tableRepository.findAll(spec);

        if (filter.getStartTime() != null && filter.getEndTime() != null) {

            List<Reservation> activeReservations = reservationRepository.findByStatus(ReservationStatus.ACTIVE);

            Map<Long, List<Reservation>> reservationsByTable = activeReservations.stream()
                    .collect(Collectors.groupingBy(r -> r.getTable().getId()));

            tables = tables.stream()
                    .filter(table -> isTableAvailable(
                            table,
                            reservationsByTable.get(table.getId()),
                            filter.getStartTime(),
                            filter.getEndTime()))
                    .toList();
        }

        return tables.stream()
                .map(this::mapToDto)
                .toList();
    }

    private boolean isTableAvailable(
            RestaurantTable table,
            List<Reservation> reservations,
            LocalDateTime startTime,
            LocalDateTime endTime) {

        if (reservations == null || reservations.isEmpty()) {
            return true;
        }

        return reservations.stream()
                .noneMatch(reservation -> overlaps(startTime, endTime,
                        reservation.getStartTime(),
                        reservation.getEndTime()));
    }

    private TableDTO mapToDto(RestaurantTable table) {
        return TableDTO.builder()
                .id(table.getId())
                .tableNumber(table.getTableNumber())
                .capacity(table.getCapacity())
                .zone(table.getZone().name())
                .posX(table.getPosX())
                .posY(table.getPosY())
                .build();
    }

    public Map<LocalDate, List<TimeSlotDTO>> getTableAvailability(Integer tableNumber) {
        RestaurantTable table = tableRepository.findAll().stream()
                .filter(t -> t.getTableNumber().equals(tableNumber))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Table not found: " + tableNumber));

        List<Reservation> reservations = table.getReservations().stream()
                .filter(r -> r.getStatus() == ReservationStatus.ACTIVE)
                .sorted(Comparator.comparing(Reservation::getStartTime))
                .toList();

        LocalTime openTime = rulesProperties.getOpenTime();
        LocalTime closeTime = rulesProperties.getCloseTime();

        int daysAhead = rulesProperties.getDaysAhead();
        Map<LocalDate, List<TimeSlotDTO>> availability = new LinkedHashMap<>();

        for (int i = 0; i <= daysAhead; i++) {
            LocalDate date = LocalDate.now().plusDays(i);
            List<TimeSlotDTO> slots = new ArrayList<>();

            LocalDateTime startCursor = date.atTime(openTime);

            List<Reservation> reservationsForDay = reservations.stream()
                    .filter(r -> r.getStartTime().toLocalDate().equals(date))
                    .toList();

            for (Reservation res : reservationsForDay) {
                LocalDateTime resStart = res.getStartTime();
                LocalDateTime resEnd = res.getEndTime();

                if (startCursor.isBefore(resStart)) {
                    slots.add(new TimeSlotDTO(startCursor, resStart));
                }

                startCursor = resEnd.isAfter(startCursor) ? resEnd : startCursor;
            }

            LocalDateTime endOfDay = date.atTime(closeTime);
            if (startCursor.isBefore(endOfDay)) {
                slots.add(new TimeSlotDTO(startCursor, endOfDay));
            }

            availability.put(date, slots);
        }

        return availability;
    }
}