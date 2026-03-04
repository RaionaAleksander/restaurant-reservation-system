package com.aleksander.restaurant.reservation.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "restaurant_tables")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer tableNumber;

    @Column(nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Zone zone;

    @Column(nullable = false)
    private Integer posX;

    @Column(nullable = false)
    private Integer posY;

    @Column(nullable = false)
    private boolean nearWindow;

    @Column(nullable = false)
    private boolean quietCorner;

    @Column(nullable = false)
    private boolean nearKidsZone;

    @Column(nullable = false)
    private boolean accessible;

    @OneToMany(mappedBy = "table", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Reservation> reservations;
}
