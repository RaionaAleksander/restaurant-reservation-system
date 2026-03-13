# Restaurant Table Reservation System

Web application for restaurant table reservation and intelligent table recommendation based on party size and user preferences.

Built with Spring Boot (Java 21).

---

## Tech Stack

- Java 21  
- Spring Boot  
- Spring Data JPA  
- PostgreSQL  
- Maven  
- Springdoc OpenAPI (Swagger)  
- Docker & Docker Compose  

---

## Project Status

- Started: 28.02.2026  
- Total time spent: 24 hours  
- Current stage: Backend foundation (database, entities, APIs, Docker)  

---

## Architecture

Project follows a layered architecture:

Controller → Service → Repository → Database

- **Controllers** handle HTTP requests  
- **Services** implement business logic  
- **Repositories** handle database operations  

---

## Restaurant Layout

The restaurant contains **40 tables** across zones:

- **Main Hall:** 22 tables  
- **Terrace:** 12 tables  
- **Private Rooms:** 6 tables  

### Table Capacities

- Private rooms: 2 tables of 5 seats, 4 tables of 6 seats  
- Main hall & terrace: 1–4 guests  

### Special Areas

- Kids zone (4 tables nearby)  
- Tables near windows / quiet corners / accessible for disabled  

### Table Coordinates

Each table has (`posX`, `posY`) for front-end rendering and future layout/visualization.  

**Diagram:**

![Restaurant Layout](src/main/resources/restaurant-layout.png)  

⚠️ If modifying layout, update both `tables-config.json` and the diagram to maintain UI consistency.

---

## How to Run

### Option 1 — Run with Docker (recommended)

1. Make sure Docker and Docker Compose are installed.

2. Clone repository

```
git clone https://github.com/RaionaAleksander/restaurant-reservation-system.git
cd restaurant-reservation-system
```

3. Start the application

```
docker-compose up --build
```

The application will start on:

```
http://localhost:8080
```

Swagger UI:

```
http://localhost:8080/swagger-ui.html
```

---

### Option 2 — Run locally

#### 1. Clone repository

```
git clone https://github.com/RaionaAleksander/restaurant-reservation-system.git
cd restaurant-reservation-system
```

#### 2. Configure database

Create PostgreSQL database:

```
restaurant_db
```

Create user:

```
restaurant_user
```

Update credentials in:

```
src/main/resources/application.properties
```

#### 3. Run application

```
mvn spring-boot:run
```

or

```
./mvnw spring-boot:run
```

App & Swagger endpoints same as above.

---

## Documentation

All project docs are in docs/:

- [Project Roadmap](docs/ROADMAP.md)

- [API Reference](docs/API.md)

- [Changelog](docs/CHANGELOG.md)

## API Highlights

### Tables API Example

```http
GET /api/v1/tables?capacity=4&nearWindow=true&startTime=2026-03-12T18:00:00&endTime=2026-03-12T19:30:00
```

### Reservation Creation Example

```http
POST /api/v1/reservations
Content-Type: application/json

{
  "tableId": 5,
  "customerName": "Alice",
  "partySize": 3,
  "startTime": "2026-03-12T18:00:00",
  "endTime": "2026-03-12T19:00:00"
}
```