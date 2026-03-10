package com.aleksander.restaurant.reservation.specification;

import com.aleksander.restaurant.reservation.model.Reservation;
import com.aleksander.restaurant.reservation.model.ReservationStatus;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;

public class ReservationSpecification {

    public static Specification<Reservation> hasStatus(ReservationStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Reservation> hasCustomerName(String customerName) {
        return (root, query, cb) -> customerName == null ? null
                : cb.like(cb.lower(root.get("customerName")),
                        "%" + customerName.toLowerCase() + "%");
    }

    public static Specification<Reservation> hasTableNumber(Integer tableNumber) {
        return (root, query, cb) -> tableNumber == null ? null
                : cb.equal(root.get("table").get("tableNumber"), tableNumber);
    }

    public static Specification<Reservation> hasDate(LocalDate date) {
        return (root, query, cb) -> date == null ? null
                : cb.equal(
                        cb.function("date", LocalDate.class, root.get("startTime")),
                        date);
    }

}