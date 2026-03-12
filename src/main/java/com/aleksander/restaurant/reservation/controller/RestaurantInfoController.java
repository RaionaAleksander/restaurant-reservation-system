package com.aleksander.restaurant.reservation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aleksander.restaurant.reservation.dto.RestaurantInfoDTO;
import com.aleksander.restaurant.reservation.service.RestaurantInfoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RestaurantInfoController {

    private final RestaurantInfoService service;

    @GetMapping("/restaurant-info")
    public RestaurantInfoDTO getRestaurantInfo() {
        return service.getRestaurantInfo();
    }
}