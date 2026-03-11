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

    private int calculateScore(RestaurantTable table, TableFilter filter) {
        int score = 0;

        // Capacity is the most important factor, so we calculate a score based on how
        // well the table capacity matches the requested party size
        int capacityDiff = table.getCapacity() - filter.getCapacity();

        if (capacityDiff < 0) {
            return 0;
        }

        int capacityScore = 50 - capacityDiff * 5;

        score += Math.max(capacityScore, 10);

        if (Boolean.TRUE.equals(filter.getNearWindow()) && table.isNearWindow())
            score += 10;
        if (Boolean.TRUE.equals(filter.getQuietCorner()) && table.isQuietCorner())
            score += 10;
        if (Boolean.TRUE.equals(filter.getNearKidsZone()) && table.isNearKidsZone())
            score += 5;
        if (Boolean.TRUE.equals(filter.getAccessible()) && table.isAccessible())
            score += 10;

        if (filter.getZone() != null && table.getZone() == filter.getZone())
            score += 5;

        return score;
    }

    public List<TableDTO> findTables(TableFilter filter, boolean recommend) {

        if (recommend && filter.getCapacity() == null) {
            throw new IllegalArgumentException(
                    "Capacity must be specified when using table recommendation");
        }

        Specification<RestaurantTable> spec = Specification.unrestricted();

        // Hard filters is capacity and zone - if they don't match, we don't want to
        // consider the table at all
        spec = spec.and(TableSpecification.hasCapacity(filter.getCapacity()));
        spec = spec.and(TableSpecification.hasZone(filter.getZone()));
        // Soft preferences is nearWindow, quietCorner, nearKidsZone, accessible - they
        // will affect the score
        // but won't exclude the table from results

        if (!recommend) {
            spec = spec.and(TableSpecification.nearWindow(filter.getNearWindow()));
            spec = spec.and(TableSpecification.nearKidsZone(filter.getNearKidsZone()));
            spec = spec.and(TableSpecification.quietCorner(filter.getQuietCorner()));
            spec = spec.and(TableSpecification.accessible(filter.getAccessible()));
        }

        List<RestaurantTable> tables = tableRepository.findAll(spec);

        List<RestaurantTable> filteredTables = tables;

        // If time range is provided, we need to filter out tables that are not
        // available in that time range
        if (filter.getStartTime() != null && filter.getEndTime() != null) {

            List<Reservation> activeReservations = reservationRepository.findByStatus(ReservationStatus.ACTIVE);

            Map<Long, List<Reservation>> reservationsByTable = activeReservations.stream()
                    .collect(Collectors.groupingBy(r -> r.getTable().getId()));

            filteredTables = tables.stream()
                    .filter(table -> isTableAvailable(
                            table,
                            reservationsByTable.get(table.getId()),
                            filter.getStartTime(),
                            filter.getEndTime()))
                    .toList();
        }

        List<TableDTO> dtos = filteredTables.stream()
                .map(this::mapToDto)
                .toList();

        // If recommend is false, we return the filtered list without scores and sorting
        if (!recommend) {
            return dtos;
        }

        // If recommend is true, we calculate scores and sort by score
        List<TableDTO> scoredTables = filteredTables.stream()
                .map(table -> {
                    TableDTO dto = mapToDto(table);
                    dto.setScore(calculateScore(table, filter));
                    return dto;
                })
                .filter(dto -> dto.getScore() > 0)
                .sorted(Comparator.comparing(TableDTO::getScore).reversed())
                .toList();

        return scoredTables;
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
        RestaurantTable table = tableRepository.findByTableNumber(tableNumber)
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