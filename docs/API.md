# API Documentation

## Base URL

All endpoints are versioned.

```
/api/v1
```

---

# Reservation API

Handles reservation creation, cancellation and search.

---

## Search Reservations

Search reservations with optional filters and pagination.

```
GET /api/v1/reservations
```

### Query Parameters

| Parameter    | Type              | Description                  |
| ------------ | ----------------- | ---------------------------- |
| status       | ReservationStatus | Filter by reservation status |
| date         | LocalDate         | Filter by reservation date   |
| customerName | String            | Search by customer name      |
| tableNumber  | Integer           | Filter by table number       |
| page         | Integer           | Page number                  |
| size         | Integer           | Page size                    |
| sort | String | Sorting field and direction (example: startTime,desc) |

### Example

```
GET /api/v1/reservations?status=ACTIVE&page=0&size=10&sort=startTime,desc
```

---

## Create Reservation

Create a reservation for a single table.

```
POST /api/v1/reservations
```

### Request Body Example

```json
{
  "tableId": 3,
  "customerName": "John Doe",
  "partySize": 4,
  "startTime": "2026-03-12T18:00:00",
  "endTime": "2026-03-12T19:30:00"
}
```

### Rules

- Reservation must be within restaurant working hours
- Reservation must be within the allowed future reservation window
- Reservation duration must respect minimum and maximum duration limits
- Reservation times must follow 15-minute time slots
- Table must exist
- Party size must not exceed table capacity
- Reservation time must not overlap with existing reservations

---

## Create Group Reservation

Create a reservation across multiple tables.

```
POST /api/v1/reservations/group
```

### Query Parameters

| Parameter    | Type          | Description       |
| ------------ | ------------- | ----------------- |
| tableIds     | List<Long>    | Tables to reserve |
| partySize    | Integer       | Total party size  |
| customerName | String        | Customer name     |
| startTime    | LocalDateTime | Reservation start |
| endTime      | LocalDateTime | Reservation end   |

### Example

```
POST /api/v1/reservations/group?tableIds=1,2&partySize=6
```

### Behavior

* Multiple reservations are created
* Each reservation references the same customer and time slot
* Used when a group requires multiple tables

---

## Cancel Reservation

Cancel an active reservation.

```
PATCH /api/v1/reservations/{id}/cancel
```

### Rules

* Only ACTIVE reservations can be cancelled
* COMPLETED reservations cannot be cancelled
* If reservation does not exist, an error is returned

### Example

```
PATCH /api/v1/reservations/5/cancel
```

---

# Table API

Provides table search and availability.

---

## Search Tables

Retrieve restaurant tables using flexible filtering.

```
GET /api/v1/tables
```

### Query Parameters

| Parameter    | Type          | Description                                   |
| ------------ | ------------- | --------------------------------------------- |
| capacity     | Integer       | Minimum required seats                        |
| zone         | Zone          | Table zone (TERRACE, MAIN_HALL, PRIVATE_ROOM) |
| nearWindow   | Boolean       | Table near window                             |
| nearKidsZone | Boolean       | Table near kids zone                          |
| quietCorner  | Boolean       | Quiet corner table                            |
| accessible   | Boolean       | Accessible for disabled guests                |
| startTime    | LocalDateTime | Reservation start                             |
| endTime      | LocalDateTime | Reservation end                               |
| recommend    | Boolean       | Enable recommendation scoring                 |

### Examples

Search by capacity:

```
GET /api/v1/tables?capacity=4
```

Filter by zone and window:

```
GET /api/v1/tables?zone=TERRACE&nearWindow=true
```

Find tables available for a time range:

```
GET /api/v1/tables?startTime=2026-03-12T18:00:00&endTime=2026-03-12T19:30:00
```

Enable recommendation scoring:

```
GET /api/v1/tables?capacity=4&recommend=true
```

### Recommendation Behavior

When `recommend=true`:

* Tables are ranked by suitability
* Score considers:

  * capacity match
  * preferred features
  * seating characteristics

---

## Table Availability

Returns available time slots for a specific table.

```
GET /api/v1/tables/{tableNumber}/availability
```

### Example

```
GET /api/v1/tables/5/availability
```

### Example Response

```json
{
  "2026-03-12": [
    {
      "startTime": "2026-03-12T09:30:00",
      "endTime": "2026-03-12T11:00:00"
    },
    {
      "startTime": "2026-03-12T12:30:00",
      "endTime": "2026-03-12T14:00:00"
    }
  ]
}
```

---

# Statistics API

Provides reservation analytics and dashboard data.

---

## Dashboard Stats

Returns general statistics.

```
GET /api/v1/stats
```

Example response:

```json
{
  "totalTables": 24,
  "totalReservations": 120,
  "activeReservations": 8,
  "todayReservations": 14,
  "cancelledReservations": 5
}
```

Returns:

* total number of tables

* total number of reservations in the system

* currently active reservations

* reservations scheduled for today

* cancelled reservations



---

## Reservations Stats

Returns reservation counts for the last N days.

```
GET /api/v1/stats/reservations
```

### Query Parameters

| Parameter | Type    | Description    |
| --------- | ------- | -------------- |
| days      | Integer | Number of days |

Example:

```
GET /api/v1/stats/reservations?days=7
```

---

## Daily Reservation Stats

Returns reservation counts grouped by day for the last N days.

The statistics include past days up to the current date.

```
GET /api/v1/stats/reservations/daily
```

Example:

```
GET /api/v1/stats/reservations/daily?days=14
```

---

# Reservation Timeline API

Provides a timeline view of reservations grouped by tables.

```
GET /api/v1/reservations/timeline
```

### Query Parameters

| Parameter | Type      | Description      |
| --------- | --------- | ---------------- |
| date      | LocalDate | Reservation date |

Example:

```
GET /api/v1/reservations/timeline?date=2026-03-12
```

Used to visualize daily reservation schedules.

### Example Response

```json
{
  "date": "2026-03-13",
  "tables": [
    {
      "tableId": 5,
      "tableNumber": 5,
      "reservations": [
        {
          "startTime": "2026-03-13T12:30:00",
          "endTime": "2026-03-13T13:30:00",
          "customerName": "John Doe"
        },
        {
          "startTime": "2026-03-13T18:00:00",
          "endTime": "2026-03-13T19:30:00",
          "customerName": "Anna Smith"
        }
      ]
    }
  ]
}
```

---

# Restaurant Info API

Provides restaurant metadata and configuration.

```
GET /api/v1/restaurant-info
```

Returns:

* restaurant zones
* number of tables
* reservation rules
* working hours

Example response:

```json
{
  "rules": {
    "openTime": "09:30",
    "closeTime": "22:15",
    "daysAhead": 7,
    "minDurationMinutes": 30,
    "maxDurationMinutes": 120
  },
  "zones": [
    "MAIN_HALL",
    "TERRACE",
    "PRIVATE_ROOM"
  ],
  "tablesCount": 24,
  "maxTableCapacity": 8
}
```

---

# Reservation Rules API

Returns reservation configuration settings.

```
GET /api/v1/reservation-rules
```

Example response:

```json
{
  "openTime": "09:30",
  "closeTime": "22:15",
  "daysAhead": 7,
  "minDurationMinutes": 30,
  "maxDurationMinutes": 120
}
```

---

# System API

---

## Health Check

Simple endpoint to verify that the service is running.

```
GET /api/v1/health
```

Example response:

```json
{
  "status": "UP"
}
```

Used by:

* monitoring tools
* container orchestration
* uptime checks
