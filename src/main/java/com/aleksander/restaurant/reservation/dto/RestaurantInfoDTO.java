package com.aleksander.restaurant.reservation.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RestaurantInfoDTO {

    private ReservationRulesDTO rules;

    private List<String> zones;

    private int tablesCount;

    private int maxTableCapacity;
}