# Restaurant Table Reservation System

Web application for restaurant table reservation and intelligent table recommendation based on party size and user preferences.

Built with Spring Boot (Java 21).

## Tech Stack

- Java 21
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Springdoc OpenAPI (Swagger)
- Maven

## Development Status 

Project started: 28.02.2026 
Current stage: Backend foundation (database + entities)

## Time Tracking 

Total time spent: 1.5 hours

## Architecture

Project follows layered architecture:

Controller → Service → Repository → Database

- Controllers handle HTTP requests
- Services contain business logic
- Repositories handle database operations

## How to Run

### 1. Clone repository
git clone [https://github.com/RaionaAleksander/restaurant-reservation-system.git](https://github.com/RaionaAleksander/restaurant-reservation-system)

### 2. Configure database

Create PostgreSQL database: **restaurant_db**

Create user: **restaurant_user**

Update **application.properties** with credentials.

### 3. Run application

mvn spring-boot:run (or ./mvnw spring-boot:run)

Application will start on: http://localhost:8080

## API Documentation

Swagger UI:
http://localhost:8080/swagger-ui.html

## Current Progress

- Project setup completed
- Database configured
- RestaurantTable entity created
- Reservation entity partially implemented

## Future Plans

- Implement table recommendation algorithm
- Add visual restaurant layout
- Add tests and Docker support
- Implement admin interface