package com.aleksander.restaurant.reservation.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.aleksander.restaurant.reservation.dto.TableDTO;
import com.aleksander.restaurant.reservation.dto.TableFilter;
import com.aleksander.restaurant.reservation.model.Reservation;
import com.aleksander.restaurant.reservation.model.RestaurantTable;
import com.aleksander.restaurant.reservation.repository.ReservationRepository;
import com.aleksander.restaurant.reservation.repository.RestaurantTableRepository;
import com.aleksander.restaurant.reservation.specification.TableSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TableService {

    private final RestaurantTableRepository tableRepository;
    private final ReservationRepository reservationRepository;

    public List<TableDTO> findTables(TableFilter filter) {

        Specification<RestaurantTable> spec = Specification.unrestricted();

        spec = spec.and(TableSpecification.hasCapacity(filter.getCapacity()));
        spec = spec.and(TableSpecification.hasMinCapacity(filter.getMinCapacity()));
        spec = spec.and(TableSpecification.hasZone(filter.getZone()));
        spec = spec.and(TableSpecification.nearWindow(filter.getNearWindow()));
        spec = spec.and(TableSpecification.nearKidsRoom(filter.getNearKidsRoom()));
        spec = spec.and(TableSpecification.quietCorner(filter.getQuietCorner()));
        spec = spec.and(TableSpecification.accessible(filter.getAccessible()));

        List<RestaurantTable> tables = tableRepository.findAll(spec);

        if (filter.getStartTime() != null && filter.getEndTime() != null) {

            tables = tables.stream()
                    .filter(table -> isTableAvailable(
                            table,
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
            LocalDateTime startTime,
            LocalDateTime endTime) {
        List<Reservation> reservations = reservationRepository.findByTableId(table.getId());

        return reservations.stream()
                .noneMatch(reservation -> startTime.isBefore(reservation.getEndTime()) &&
                        endTime.isAfter(reservation.getStartTime()));
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
}