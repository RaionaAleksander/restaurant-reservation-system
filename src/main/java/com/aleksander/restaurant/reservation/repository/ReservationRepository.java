package com.aleksander.restaurant.reservation.repository;

import com.aleksander.restaurant.reservation.model.Reservation;
import com.aleksander.restaurant.reservation.model.ReservationStatus;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {
    List<Reservation> findByTableId(Long tableId);

    List<Reservation> findByTableIdAndStatus(Long tableId, ReservationStatus status);

    List<Reservation> findByStatus(ReservationStatus status);

    long countByStatus(ReservationStatus status);

    long countByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT r.startTime, COUNT(r) " +
            "FROM Reservation r " +
            "WHERE r.startTime >= :from AND r.startTime < :to " +
            "GROUP BY r.startTime")
    List<Object[]> countReservationsGroupedByDate(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}