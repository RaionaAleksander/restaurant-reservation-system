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
import java.util.List;
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

        tableRepository.saveAll(tables);

        generateRandomReservations();
    }

    private void generateRandomReservations() {

        int targetReservations = generatorProperties.getCount();
        if (targetReservations <= 0)
            return;

        List<RestaurantTable> tables = tableRepository.findAll();
        Random random = new Random();
        int created = 0;

        List<LocalTime> timeSlots = generateTimeSlots(rulesProperties.getOpenTime(), rulesProperties.getCloseTime(),
                rulesProperties.getMinDuration());

        while (created < targetReservations) {
            RestaurantTable table = tables.get(random.nextInt(tables.size()));

            LocalDate date = LocalDate.now()
                    .plusDays(1 + random.nextInt(rulesProperties.getDaysAhead()));

            LocalTime startSlot = timeSlots.get(random.nextInt(timeSlots.size()));
            LocalDateTime startTime = date.atTime(startSlot);

            long minMinutes = rulesProperties.getMinDuration().toMinutes();
            long maxMinutes = rulesProperties.getMaxDuration().toMinutes();

            int minSlots = (int) (minMinutes / 15);
            int maxSlots = (int) (maxMinutes / 15);

            int durationSlots = minSlots + random.nextInt(maxSlots - minSlots + 1);
            long durationMinutes = durationSlots * 15;

            LocalDateTime endTime = startTime.plusMinutes(durationMinutes);

            if (endTime.toLocalTime().isAfter(rulesProperties.getCloseTime())) {
                continue;
            }

            boolean conflict = reservationRepository
                    .findByTableId(table.getId())
                    .stream()
                    .anyMatch(existing -> startTime.isBefore(existing.getEndTime()) &&
                            endTime.isAfter(existing.getStartTime()));

            if (!conflict) {

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

                reservationRepository.save(reservation);
                created++;
            }
        }

        System.out.println("Generated " + created + " random reservations");
    }

    private List<LocalTime> generateTimeSlots(LocalTime openTime, LocalTime closeTime, Duration minDuration) {

        List<LocalTime> slots = new ArrayList<>();
        LocalTime current = openTime;
        LocalTime lastPossibleStart = closeTime.minus(minDuration);

        while (!current.isAfter(lastPossibleStart)) {
            slots.add(current);
            current = current.plusMinutes(15);
        }

        return slots;
    }

}
