package com.aleksander.restaurant.reservation.service;

import com.aleksander.restaurant.reservation.config.ReservationRulesProperties;
import com.aleksander.restaurant.reservation.dto.PageResponse;
import com.aleksander.restaurant.reservation.dto.ReservationDTO;
import com.aleksander.restaurant.reservation.dto.ReservationFilter;
import com.aleksander.restaurant.reservation.model.Reservation;
import com.aleksander.restaurant.reservation.model.ReservationStatus;
import com.aleksander.restaurant.reservation.model.RestaurantTable;
import com.aleksander.restaurant.reservation.repository.ReservationRepository;
import com.aleksander.restaurant.reservation.repository.RestaurantTableRepository;
import com.aleksander.restaurant.reservation.specification.ReservationSpecification;

import static com.aleksander.restaurant.reservation.util.ReservationTimeUtils.overlaps;
import lombok.RequiredArgsConstructor;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RestaurantTableRepository tableRepository;
    private final ReservationRulesProperties rulesProperties;

    public PageResponse<ReservationDTO> findReservations(ReservationFilter filter, Pageable pageable) {
        Specification<Reservation> spec = Specification.unrestricted();

        spec = spec.and(ReservationSpecification.hasStatus(filter.getStatus()));
        spec = spec.and(ReservationSpecification.hasCustomerName(filter.getCustomerName()));
        spec = spec.and(ReservationSpecification.hasTableNumber(filter.getTableNumber()));
        spec = spec.and(ReservationSpecification.hasDate(filter.getDate()));

        Page<Reservation> pageResult = reservationRepository.findAll(spec, Objects.requireNonNull(pageable));

        return PageResponse.from(pageResult, this::mapToDto);
    }

    private ReservationDTO mapToDto(Reservation reservation) {
        return ReservationDTO.builder()
                .id(reservation.getId())
                .tableId(reservation.getTable().getId())
                .tableNumber(reservation.getTable().getTableNumber())
                .customerName(reservation.getCustomerName())
                .partySize(reservation.getPartySize())
                .startTime(reservation.getStartTime())
                .endTime(reservation.getEndTime())
                .status(reservation.getStatus())
                .build();
    }

    public ReservationDTO createReservation(Long tableId,
            String customerName,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer partySize) {

        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        if (startTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Reservation cannot start in the past");
        }

        LocalTime startTimeOnly = startTime.toLocalTime();
        LocalTime endTimeOnly = endTime.toLocalTime();

        if (startTimeOnly.isBefore(rulesProperties.getOpenTime()) ||
                endTimeOnly.isAfter(rulesProperties.getCloseTime())) {
            throw new IllegalArgumentException("Reservation must be within restaurant working hours");
        }

        LocalDate maxDate = LocalDate.now().plusDays(rulesProperties.getDaysAhead());

        if (startTime.toLocalDate().isAfter(maxDate)) {
            throw new IllegalArgumentException("Reservation date is too far in future");
        }

        long durationMinutes = Duration.between(startTime, endTime).toMinutes();

        if (durationMinutes < rulesProperties.getMinDuration().toMinutes() ||
                durationMinutes > rulesProperties.getMaxDuration().toMinutes()) {
            throw new IllegalArgumentException("Invalid reservation duration");
        }

        if (startTime.getMinute() % 15 != 0 || endTime.getMinute() % 15 != 0) {
            throw new IllegalArgumentException("Reservation time must be in 15-minute intervals");
        }

        RestaurantTable table = tableRepository.findById(Objects.requireNonNull(tableId))
                .orElseThrow(() -> new IllegalArgumentException("Table not found"));

        if (partySize > table.getCapacity()) {
            throw new IllegalArgumentException("Party size exceeds table capacity");
        }

        List<Reservation> existingReservations = table.getReservations();

        for (Reservation existing : existingReservations) {
            if (existing.getStatus() == ReservationStatus.ACTIVE &&
                    overlaps(startTime, endTime,
                            existing.getStartTime(),
                            existing.getEndTime())) {

                throw new IllegalArgumentException("Table is already reserved for this time slot");
            }
        }

        Reservation reservation = Reservation.builder()
                .table(table)
                .customerName(customerName)
                .startTime(startTime)
                .endTime(endTime)
                .partySize(partySize)
                .status(ReservationStatus.ACTIVE)
                .build();

        reservationRepository.save(Objects.requireNonNull(reservation));

        return mapToDto(reservation);
    }

    @Transactional
    public void cancelReservation(Long reservationId) {

        Reservation reservation = reservationRepository.findById(Objects.requireNonNull(reservationId))
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found with id: " + reservationId));

        if (reservation.getStatus() == ReservationStatus.COMPLETED) {
            throw new IllegalStateException("Completed reservation cannot be cancelled");
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("Reservation is already cancelled");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);

        reservationRepository.save(reservation);
    }

    @Transactional
    public List<ReservationDTO> createGroupReservation(
            List<Long> tableIds,
            String customerName,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer partySize) {

        if (tableIds == null || tableIds.size() < 2) {
            throw new IllegalArgumentException("At least two table IDs are required for group reservation");
        }

        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        if (startTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Reservation cannot start in the past");
        }

        Long parentId = null;

        List<ReservationDTO> createdReservations = new ArrayList<>();

        for (Long tableId : tableIds) {
            RestaurantTable table = tableRepository.findById(Objects.requireNonNull(tableId))
                    .orElseThrow(() -> new IllegalArgumentException("Table not found with id: " + tableId));

            int totalCapacity = tableIds.stream()
                    .map(tableRepository::findById)
                    .filter(Optional::isPresent)
                    .mapToInt(opt -> opt.get().getCapacity())
                    .sum();

            if (partySize > totalCapacity) {
                throw new IllegalArgumentException("Party size exceeds total capacity of selected tables");
            }

            List<Reservation> existingReservations = reservationRepository.findByTableId(tableId);
            boolean conflict = existingReservations.stream()
                    .anyMatch(r -> r.getStatus() == ReservationStatus.ACTIVE &&
                            overlaps(startTime, endTime, r.getStartTime(), r.getEndTime()));

            if (conflict) {
                throw new IllegalArgumentException(
                        "Table " + table.getTableNumber() + " is already reserved for this time slot");
            }

            Reservation reservation = Reservation.builder()
                    .table(table)
                    .customerName(customerName)
                    .startTime(startTime)
                    .endTime(endTime)
                    .partySize(partySize)
                    .status(ReservationStatus.ACTIVE)
                    .parentReservationId(parentId)
                    .build();

            reservationRepository.save(Objects.requireNonNull(reservation));

            if (parentId == null) {
                parentId = reservation.getId();
                reservation.setParentReservationId(parentId);
                reservationRepository.save(reservation);
            }

            createdReservations.add(mapToDto(reservation));
        }

        return createdReservations;
    }
}
