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
- Optimized random reservation generation using internal table availability structure to avoid repeated conflict checks.
- Added support for generating reservations starting from the current day if sufficient time remains before restaurant closing.
- Improved performance of reservation generator for larger datasets.

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
- Added global CORS configuration to allow frontend access from http://localhost:3000

---

## Reservation Generation Improvements

- Reservation generation now uses 15-minute time slots
- Reservation generation respects restaurant working hours
- Fixed reservation time generation to respect opening minutes
- Refactored reservation generator to use precomputed valid time slots
- Added generation of completed (past) reservations with status COMPLETED
- Added configuration for past completed reservations: `reservation.generator.past-days` for number of past days to generate

---

## Table Management

- Added `GET /api/tables` endpoint
- Implemented dynamic table filtering using Spring Data JPA Specifications
- Simplified table capacity filtering: capacity now represents minimum required seats
- Availability checks consider only ACTIVE reservations

---

## Table Recommendation System

- Added recommendation scoring algorithm to table search API. When recommend=true, available tables are ranked based on capacity fit and preference attributes (zone, window proximity, quiet corner, kids zone, accessibility).
- Refactored table recommendation logic by separating hard filters and preference-based scoring to improve recommendation accuracy.
- Added validation to ensure capacity is provided when using recommendation mode to prevent invalid scoring calculations.

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
- Added factory method in PageResponse to simplify conversion from Spring Page to API response.
- Updated reservation creation endpoint to return ReservationDTO instead of entity.
- Added statistics endpoint (GET /api/stats) returning total counts of tables and reservations.
- Extended Stats API (Dashboard) to include: total tables, total reservations, active, cancelled, and today's reservations.
- Added /api/stats/reservations endpoint with `days` parameter to get reservation count for the last N days.
- Added /api/stats/reservations/daily?days= endpoint with `days` parameter to get daily reservation counts for the past N days
- Added API endpoint to retrieve available reservation time slots for a specific table (`GET /api/tables/{tableNumber}/availability`) based on restaurant working hours and existing active reservations.
- Fixed /api/stats/reservations to count only COMPLETED reservations for current day
- Refactored /api/stats/reservations and /api/stats/reservations/daily to filter COMPLETED reservations at repository level
- Updated Swagger documentation to clarify that capacity parameter is required when using recommendation mode.
- Added group/double reservation API (`POST /api/reservations/group`) allowing to reserve multiple tables simultaneously for the same customer and time slot. Introduced `parentReservationId` field in Reservation and ReservationDTO to link group reservations.
- Added API endpoint GET /api/reservation-rules to expose restaurant reservation configuration (working hours, booking window, and duration limits) for frontend usage.
- Added GET /api/restaurant-info endpoint to provide aggregated restaurant configuration including reservation rules, zones, and table statistics for frontend initialization.
- Added GET /api/reservations/timeline endpoint to return daily reservation schedule grouped by tables for timeline visualization.
- Added GET /api/health endpoint to provide a simple service health check returning application status.

---

## Infrastructure Improvements

- Added Docker support: Dockerfile and docker-compose.yml for backend + Postgres
- Added Docker multi-stage build configuration for backend using Maven and Eclipse Temurin JDK 21
- Fixed Docker database password configuration by quoting the value in docker-compose.yml to properly handle special characters (e.g. '!') in environment variables.
- Added .dockerignore to exclude unnecessary files (git metadata, build artifacts, IDE files) from Docker build context to improve build performance and reduce image size.

---

## Bug Fixes

- Fixed infinite JSON recursion in bidirectional JPA relationship