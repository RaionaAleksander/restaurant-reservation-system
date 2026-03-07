package com.aleksander.restaurant.reservation.service;

import com.aleksander.restaurant.reservation.config.ReservationRulesProperties;
import com.aleksander.restaurant.reservation.model.Reservation;
import com.aleksander.restaurant.reservation.model.ReservationStatus;
import com.aleksander.restaurant.reservation.model.RestaurantTable;
import com.aleksander.restaurant.reservation.repository.ReservationRepository;
import com.aleksander.restaurant.reservation.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RestaurantTableRepository tableRepository;
    private final ReservationRulesProperties rulesProperties;

    public Reservation createReservation(Long tableId,
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

        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("Table not found"));

        if (partySize > table.getCapacity()) {
            throw new IllegalArgumentException("Party size exceeds table capacity");
        }

        List<Reservation> existingReservations = table.getReservations();

        for (Reservation existing : existingReservations) {
            if (existing.getStatus() == ReservationStatus.ACTIVE &&
                    startTime.isBefore(existing.getEndTime()) &&
                    endTime.isAfter(existing.getStartTime())) {

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

        return reservationRepository.save(reservation);
    }
}
