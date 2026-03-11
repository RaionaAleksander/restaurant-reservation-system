package com.aleksander.restaurant.reservation.config;

import com.aleksander.restaurant.reservation.model.Reservation;
import com.aleksander.restaurant.reservation.model.ReservationStatus;
import com.aleksander.restaurant.reservation.model.RestaurantTable;
import com.aleksander.restaurant.reservation.repository.ReservationRepository;
import com.aleksander.restaurant.reservation.repository.RestaurantTableRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RestaurantTableRepository tableRepository;
    private final ReservationRepository reservationRepository;
    private final EntityManager entityManager;
    private final ObjectMapper objectMapper;
    private final ReservationGeneratorProperties generatorProperties;
    private final ReservationRulesProperties rulesProperties;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        // Clear database (reservations, restaurant_tables)
        entityManager.createNativeQuery(
                "TRUNCATE TABLE reservations, restaurant_tables RESTART IDENTITY CASCADE").executeUpdate();

        // Load table config
        InputStream inputStream = new ClassPathResource("tables-config.json").getInputStream();

        List<RestaurantTable> tables = objectMapper.readValue(inputStream,
                new TypeReference<List<RestaurantTable>>() {
                });

        tableRepository.saveAll(Objects.requireNonNull(tables));

        generateRandomReservations(tables);
        generateCompletedReservations(tables);
    }

    // Generate some completed reservations in the past for testing stats
    private void generateRandomReservations(List<RestaurantTable> tables) {

        int targetReservations = generatorProperties.getCount();
        if (targetReservations <= 0)
            return;

        Random random = new Random();
        int created = 0;

        Map<Long, Map<LocalDate, List<FreeSlot>>> availability = initializeAvailability(tables);

        while (created < targetReservations) {

            RestaurantTable table = tables.get(random.nextInt(tables.size()));

            Map<LocalDate, List<FreeSlot>> tableDays = availability.get(table.getId());

            if (tableDays.isEmpty())
                continue;

            List<LocalDate> dates = new ArrayList<>(tableDays.keySet());

            LocalDate date = dates.get(random.nextInt(dates.size()));

            List<FreeSlot> slots = tableDays.get(date);

            if (slots == null || slots.isEmpty())
                continue;

            FreeSlot slot = slots.get(random.nextInt(slots.size()));

            long slotMinutes = Duration.between(slot.getStart(), slot.getEnd()).toMinutes();

            if (slotMinutes < rulesProperties.getMinDuration().toMinutes()) {
                slots.remove(slot);
                continue;
            }

            long minMinutes = rulesProperties.getMinDuration().toMinutes();
            long maxMinutes = Math.min(rulesProperties.getMaxDuration().toMinutes(), slotMinutes);

            int minSlots = (int) (minMinutes / 15);
            int maxSlots = (int) (maxMinutes / 15);

            int durationSlots = minSlots + random.nextInt(maxSlots - minSlots + 1);
            long durationMinutes = durationSlots * 15;

            long maxStartOffset = slotMinutes - durationMinutes;
            int offsetSlots = 0;
            if (maxStartOffset > 0) {
                offsetSlots = random.nextInt((int) (maxStartOffset / 15) + 1);
            }

            LocalDateTime startTime = slot.getStart().plusMinutes(offsetSlots * 15);
            LocalDateTime endTime = startTime.plusMinutes(durationMinutes);

            if (endTime.isAfter(slot.getEnd())) {
                continue;
            }

            Reservation reservation = Reservation.builder()
                    .table(table)
                    .customerName("Guest " + (created + 1))
                    .partySize(Math.min(
                            table.getCapacity(),
                            1 + random.nextInt(table.getCapacity())))
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(ReservationStatus.ACTIVE)
                    .build();

            reservationRepository.save(Objects.requireNonNull(reservation));

            updateAvailability(slots, slot, startTime, endTime);

            created++;
        }

        System.out.println("Generated " + created + " random reservations");
    }

    private Map<Long, Map<LocalDate, List<FreeSlot>>> initializeAvailability(
            List<RestaurantTable> tables) {

        Map<Long, Map<LocalDate, List<FreeSlot>>> availability = new HashMap<>();

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        int daysAhead = rulesProperties.getDaysAhead();

        LocalTime openTime = rulesProperties.getOpenTime();
        LocalTime closeTime = rulesProperties.getCloseTime();

        for (RestaurantTable table : tables) {

            Map<LocalDate, List<FreeSlot>> tableDays = new HashMap<>();

            for (int i = 0; i <= daysAhead; i++) {

                LocalDate date = today.plusDays(i);

                LocalDateTime start = date.atTime(openTime);
                LocalDateTime end = date.atTime(closeTime);

                if (date.equals(today)) {

                    if (now.isAfter(end.minusMinutes(30))) {
                        continue;
                    }

                    if (now.isAfter(start)) {
                        LocalDateTime adjustedNow = now.withSecond(0).withNano(0);

                        int remainder = adjustedNow.getMinute() % 15;
                        if (remainder != 0) {
                            adjustedNow = adjustedNow.plusMinutes(15 - remainder); // используем adjustedNow, а не now
                        }

                        start = adjustedNow;
                    }
                }

                tableDays.put(
                        date,
                        new ArrayList<>(List.of(new FreeSlot(start, end))));
            }

            availability.put(table.getId(), tableDays);
        }

        return availability;
    }

    // Generate some completed reservations in the past for testing stats
    private void generateCompletedReservations(List<RestaurantTable> tables) {
        int targetCompleted = generatorProperties.getCountCompleted();
        if (targetCompleted <= 0)
            return;

        Random random = new Random();
        int created = 0;

        Map<Long, Map<LocalDate, List<FreeSlot>>> availability = buildAvailabilityForPast(tables);

        LocalDate today = LocalDate.now();

        while (created < targetCompleted) {
            RestaurantTable table = tables.get(random.nextInt(tables.size()));
            Map<LocalDate, List<FreeSlot>> tableDays = availability.get(table.getId());
            if (tableDays.isEmpty())
                continue;

            List<LocalDate> dates = new ArrayList<>(tableDays.keySet());
            LocalDate date = dates.get(random.nextInt(dates.size()));

            List<FreeSlot> slots = tableDays.get(date);
            if (slots == null || slots.isEmpty())
                continue;

            FreeSlot slot = slots.get(random.nextInt(slots.size()));

            long slotMinutes = Duration.between(slot.getStart(), slot.getEnd()).toMinutes();
            if (slotMinutes < rulesProperties.getMinDuration().toMinutes()) {
                slots.remove(slot);
                continue;
            }

            long minMinutes = rulesProperties.getMinDuration().toMinutes();
            long maxMinutes = Math.min(rulesProperties.getMaxDuration().toMinutes(), slotMinutes);

            int minSlots = (int) (minMinutes / 15);
            int maxSlots = (int) (maxMinutes / 15);

            int durationSlots = minSlots + random.nextInt(maxSlots - minSlots + 1);
            long durationMinutes = durationSlots * 15;

            long maxStartOffset = slotMinutes - durationMinutes;
            int offsetSlots = 0;
            if (maxStartOffset > 0) {
                offsetSlots = random.nextInt((int) (maxStartOffset / 15) + 1);
            }

            LocalDateTime startTime = slot.getStart().plusMinutes(offsetSlots * 15);
            LocalDateTime endTime = startTime.plusMinutes(durationMinutes);

            if (date.equals(today) && endTime.isAfter(LocalDateTime.now())) {
                continue;
            }

            Reservation reservation = Reservation.builder()
                    .table(table)
                    .customerName("Guest " + (created + 1))
                    .partySize(Math.min(1 + random.nextInt(table.getCapacity()), table.getCapacity()))
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(ReservationStatus.COMPLETED)
                    .build();

            reservationRepository.save(Objects.requireNonNull(reservation));
            created++;

            updateAvailability(slots, slot, startTime, endTime);
        }
    }

    private Map<Long, Map<LocalDate, List<FreeSlot>>> buildAvailabilityForPast(List<RestaurantTable> tables) {
        Map<Long, Map<LocalDate, List<FreeSlot>>> availability = new HashMap<>();
        LocalDate today = LocalDate.now();
        int pastDays = 10;

        for (RestaurantTable table : tables) {
            Map<LocalDate, List<FreeSlot>> tableDays = new HashMap<>();
            for (int i = 0; i < pastDays; i++) {
                LocalDate date = today.minusDays(i);
                LocalDateTime start = date.atTime(rulesProperties.getOpenTime());
                LocalDateTime end = date.atTime(rulesProperties.getCloseTime());

                if (date.equals(today) && LocalDateTime.now().isBefore(end)) {
                    end = LocalDateTime.now().withSecond(0).withNano(0);
                }

                tableDays.put(date, new ArrayList<>(List.of(new FreeSlot(start, end))));
            }
            availability.put(table.getId(), tableDays);
        }

        return availability;
    }

    // Helper method to update availability slots after creating a reservation
    private void updateAvailability(
            List<FreeSlot> slots,
            FreeSlot original,
            LocalDateTime reservationStart,
            LocalDateTime reservationEnd) {

        slots.remove(original);

        if (original.getStart().isBefore(reservationStart)) {

            slots.add(new FreeSlot(
                    original.getStart(),
                    reservationStart));
        }

        if (reservationEnd.isBefore(original.getEnd())) {

            slots.add(new FreeSlot(
                    reservationEnd,
                    original.getEnd()));
        }
    }

    // Helper class to represent free time slots for reservation generation
    @Getter
    @AllArgsConstructor
    private static class FreeSlot {

        private LocalDateTime start;
        private LocalDateTime end;
    }
}
