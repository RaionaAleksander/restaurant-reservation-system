package com.aleksander.restaurant.reservation.config;

import com.aleksander.restaurant.reservation.model.RestaurantTable;
import com.aleksander.restaurant.reservation.repository.RestaurantTableRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RestaurantTableRepository tableRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {

        if (tableRepository.count() > 0) {
            return;
        }

        InputStream inputStream = new ClassPathResource("tables-config.json").getInputStream();

        List<RestaurantTable> tables = objectMapper.readValue(inputStream,
                new TypeReference<List<RestaurantTable>>() {
                });

        tableRepository.saveAll(tables);
    }
}
