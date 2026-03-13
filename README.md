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

Total time spent: 23 hours

## Architecture

Project follows layered architecture:

Controller → Service → Repository → Database

- Controllers handle HTTP requests
- Services contain business logic
- Repositories handle database operations

## Restaurant Layout

The application uses a predefined restaurant layout loaded from `tables-config.json` during application startup.

The restaurant contains **40 tables** distributed across different zones:

- **Main Hall:** 22 tables  
- **Terrace:** 12 tables  
- **Private Rooms:** 6 tables  

### Seating Capacity

- Private room tables:
  - 2 tables with capacity **5**
  - 4 tables with capacity **6**
- Main hall and terrace tables:
  - Capacity between **1 and 4 guests**

### Special Areas

- A **kids zone** is present in the restaurant.
- **4 tables** are located near the kids zone.

### Coordinates

Each table has spatial coordinates (`posX`, `posY`) representing its position in the restaurant layout.  
These coordinates allow the system to support future features such as:

- visual table layout
- intelligent table recommendation
- dynamic table grouping

### Restaurant Layout Diagram

Below is a conceptual layout of the restaurant:

![Restaurant Layout](src/main/resources/restaurant-layout.png)

### Layout Consistency Notice

The restaurant layout is defined by two components:

1. The layout image (`restaurant-layout.png`)
2. The table configuration file (`tables-config.json`)

The frontend renders tables on top of the layout image using the coordinates (`posX`, `posY`) defined in the configuration file.

⚠️ If the restaurant layout is modified, both the image and the table configuration must be updated accordingly.
Otherwise, table positions displayed in the UI may not match the actual layout.

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

Application will start on:

```
http://localhost:8080
```

Swagger UI:

```
http://localhost:8080/swagger-ui.html
```

## Documentation

Project documentation is available in the `docs` folder:

- Project changelog: `docs/CHANGELOG.md`
- Project roadmap: `docs/ROADMAP.md`

## API Documentation

Swagger UI:
http://localhost:8080/swagger-ui.html

Full API documentation is available in: [API Documentation](docs/API.md)