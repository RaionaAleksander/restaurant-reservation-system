package com.aleksander.restaurant.reservation.util;

import java.time.LocalDateTime;

public final class ReservationTimeUtils {

    private ReservationTimeUtils() {
    }

    public static boolean overlaps(
            LocalDateTime start1,
            LocalDateTime end1,
            LocalDateTime start2,
            LocalDateTime end2) {

        return start1.isBefore(end2) && end1.isAfter(start2);
    }
}