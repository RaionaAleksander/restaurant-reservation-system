package com.aleksander.restaurant.reservation.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.aleksander.restaurant.reservation.dto.RestaurantInfoDTO;
import com.aleksander.restaurant.reservation.model.RestaurantTable;
import com.aleksander.restaurant.reservation.model.Zone;
import com.aleksander.restaurant.reservation.repository.RestaurantTableRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RestaurantInfoService {

    private final ReservationRulesService rulesService;
    private final RestaurantTableRepository tableRepository;

    public RestaurantInfoDTO getRestaurantInfo() {

        List<RestaurantTable> tables = tableRepository.findAll();

        int maxCapacity = tables.stream()
                .mapToInt(RestaurantTable::getCapacity)
                .max()
                .orElse(0);

        List<String> zones = Arrays.stream(Zone.values())
                .map(Enum::name)
                .toList();

        return RestaurantInfoDTO.builder()
                .rules(rulesService.getRules())
                .zones(zones)
                .tablesCount(tables.size())
                .maxTableCapacity(maxCapacity)
                .build();
    }
}