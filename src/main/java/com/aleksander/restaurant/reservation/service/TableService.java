package com.aleksander.restaurant.reservation.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.aleksander.restaurant.reservation.dto.TableDTO;
import com.aleksander.restaurant.reservation.repository.RestaurantTableRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TableService {

    private final RestaurantTableRepository tableRepository;

    public List<TableDTO> getAllTables() {

        return tableRepository.findAll()
                .stream()
                .map(table -> TableDTO.builder()
                        .id(table.getId())
                        .tableNumber(table.getTableNumber())
                        .capacity(table.getCapacity())
                        .zone(table.getZone().name())
                        .posX(table.getPosX())
                        .posY(table.getPosY())
                        .build())
                .toList();
    }
}