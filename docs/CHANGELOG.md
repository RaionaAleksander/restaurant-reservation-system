# Changelog

All notable changes to this project are documented here.

---

## Project Setup

- Project setup completed
- Database configured
- RestaurantTable entity created
- Reservation entity implemented
- ReservationRepository created

---

## Reservation System

- ReservationService implemented with business validation
- ReservationController created with POST endpoint
- Reservation flow tested via Swagger
- Added validation to prevent creating reservations in the past
- Added validation to ensure reservations follow restaurant working hours
- Added validation to prevent reservations beyond allowed future days range
- Added validation for minimum and maximum reservation duration limits
- Added validation to ensure reservation times follow 15-minute scheduling slots
- Fixed validation for reservation time intervals
- Refactored reservation time overlap logic into a shared utility method

---

## Error Handling

- GlobalExceptionHandler added for proper API error responses (400 instead of 500)
- Added exception handling for IllegalStateException (409 Conflict)

---

## Database Initialization

- Restaurant layout initialized from configuration file (`tables-config.json`)
- DataInitializer implemented to populate restaurant tables on application startup
- Random reservation generation added (50 reservations on startup)
- Database tables and reservations automatically reset and regenerated on startup

---

## Configuration Improvements

- Added configurable random reservation generator
- Reservation generation parameters externalized into `reservation-generator.properties`
- Separated reservation configuration into `reservation.properties`
- Migrated configuration binding to `@ConfigurationPropertiesScan` (Spring Boot 3 best practice)
- Configured default pagination settings for Spring Data Web:
  - Default page size: 20
  - Maximum page size: 100
  - Page indexing starts from 0 (`one-indexed-parameters=false`)

---

## Reservation Generation Improvements

- Reservation generation now uses 15-minute time slots
- Reservation generation respects restaurant working hours
- Fixed reservation time generation to respect opening minutes
- Refactored reservation generator to use precomputed valid time slots

---

## Table Management

- Added `GET /api/tables` endpoint
- Implemented dynamic table filtering using Spring Data JPA Specifications
- Simplified table capacity filtering: capacity now represents minimum required seats
- Availability checks consider only ACTIVE reservations

---

## Performance Improvements

- Optimized table availability search by eliminating N+1 queries
- Reservations are now preloaded and grouped by tableId

---

## API Improvements

- Added Swagger parameter documentation
- Improved Swagger documentation using `ParameterObject`
- Added API endpoint to cancel reservations (`PATCH /api/reservations/{id}/cancel`)
- Added API endpoint to retrieve all reservations (`GET /api/reservations`)
- Introduced ReservationDTO and updated reservation API to return DTO instead of entity.
- Added reservation filtering API using Spring Data JPA Specifications (status, date, customerName, tableNumber).
- Added pagination support (page, size) to reservation search API.
- Introduced a generic PageResponse<T> wrapper for paginated API responses and applied it to the reservation search endpoint.

---

## Bug Fixes

- Fixed infinite JSON recursion in bidirectional JPA relationship