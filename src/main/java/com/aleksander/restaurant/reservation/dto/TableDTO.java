package com.aleksander.restaurant.reservation.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TableDTO {

    private Long id;
    private Integer tableNumber;
    private Integer capacity;
    private String zone;
    private Integer posX;
    private Integer posY;

}