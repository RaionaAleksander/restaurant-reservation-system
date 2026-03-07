package com.aleksander.restaurant.reservation.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:reservation.properties")
@EnableConfigurationProperties({
        ReservationGeneratorProperties.class,
        ReservationRulesProperties.class
})
public class ReservationConfig {
}