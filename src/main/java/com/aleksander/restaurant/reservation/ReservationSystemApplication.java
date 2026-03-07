package com.aleksander.restaurant.reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ReservationSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationSystemApplication.class, args);
	}

}
