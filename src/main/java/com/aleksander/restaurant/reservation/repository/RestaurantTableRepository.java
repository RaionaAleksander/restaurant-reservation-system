package com.aleksander.restaurant.reservation.repository;

import com.aleksander.restaurant.reservation.model.RestaurantTable;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantTableRepository
        extends JpaRepository<RestaurantTable, Long>, JpaSpecificationExecutor<RestaurantTable> {

    Optional<RestaurantTable> findByTableNumber(Integer tableNumber);
}