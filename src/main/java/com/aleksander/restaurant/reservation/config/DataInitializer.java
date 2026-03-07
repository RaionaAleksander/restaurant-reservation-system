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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RestaurantTableRepository tableRepository;
    private final ReservationRepository reservationRepository;
    private final EntityManager entityManager;
    private final ObjectMapper objectMapper;

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

        List<RestaurantTable> tables = tableRepository.findAll();

        Random random = new Random();
        int created = 0;

        while (created < 50) {

            RestaurantTable table = tables.get(random.nextInt(tables.size()));

            LocalDate date = LocalDate.now().plusDays(1 + random.nextInt(7));

            int startHour = 12 + random.nextInt(8);
            int duration = 1 + random.nextInt(3);

            LocalDateTime startTime = date.atTime(startHour, 0);
            LocalDateTime endTime = startTime.plusHours(duration);

            boolean conflict = reservationRepository
                    .findByTableId(table.getId())
                    .stream()
                    .anyMatch(existing -> startTime.isBefore(existing.getEndTime()) &&
                            endTime.isAfter(existing.getStartTime()));

            if (!conflict) {

                Reservation reservation = Reservation.builder()
                        .table(table)
                        .customerName("Generated Guest " + created)
                        .partySize(Math.min(table.getCapacity(), 1 + random.nextInt(table.getCapacity())))
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
}
