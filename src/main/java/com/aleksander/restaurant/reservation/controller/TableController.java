package com.aleksander.restaurant.reservation.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aleksander.restaurant.reservation.dto.TableDTO;
import com.aleksander.restaurant.reservation.dto.TableFilter;
import com.aleksander.restaurant.reservation.dto.table.TimeSlotDTO;
import com.aleksander.restaurant.reservation.service.TableService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tables")
@RequiredArgsConstructor
public class TableController {

    private final TableService tableService;

    @GetMapping
    public ResponseEntity<List<TableDTO>> getTables(
            @ParameterObject TableFilter filter,

            @Parameter(description = "Number of guests. Required when recommend=true", example = "4") @RequestParam(required = false) Integer capacity,

            @Parameter(description = "Enable table recommendation ranking", example = "true") @RequestParam(defaultValue = "false") boolean recommend) {

        return ResponseEntity.ok(
                tableService.findTables(filter, recommend));
    }

    @GetMapping("/{tableNumber}/availability")
    @Operation(summary = "Get table availability", description = "Returns available reservation time slots for a specific table within restaurant working hours")
    public ResponseEntity<Map<LocalDate, List<TimeSlotDTO>>> getTableAvailability(
            @PathVariable Integer tableNumber) {
        return ResponseEntity.ok(tableService.getTableAvailability(tableNumber));
    }
}
