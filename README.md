# Trading Signal Tracking Application

## Overview

Trading Signal Tracking Application is a backend project developed using Java and Spring Boot. It allows users to create and manage trading signals, validate BUY/SELL rules, fetch live cryptocurrency prices from the Binance Public API, and calculate signal status and ROI.

This project was developed as part of a Backend Skill Evaluation.

---

## Tech Stack

* Java 17
* Spring Boot
* Spring Data JPA
* MySQL
* Maven
* Swagger (OpenAPI)
* Binance Public API
* JUnit 5
* Mockito

---

## Features

* Create Trading Signal
* Get All Trading Signals
* Get Trading Signal by ID
* Delete Trading Signal
* Check Live Signal Status
* BUY / SELL Business Validation
* Live Binance Price Integration
* ROI Calculation
* Global Exception Handling
* Unit Testing

---

## API Endpoints

| Method | Endpoint                 | Description                 |
| ------ | ------------------------ | --------------------------- |
| POST   | /api/signals             | Create a new trading signal |
| GET    | /api/signals             | Get all signals             |
| GET    | /api/signals/{id}        | Get signal by ID            |
| DELETE | /api/signals/{id}        | Delete signal               |
| GET    | /api/signals/{id}/status | Fetch live status and ROI   |

---

## Database

Database: MySQL

Database Name:

trading_signal_db

---

## Running the Project

1. Clone the repository
2. Create MySQL database named `trading_signal_db`
3. Update database username and password in `application.properties`
4. Run:

```bash
mvn spring-boot:run
```

Swagger UI:

```
http://localhost:8080/swagger-ui/index.html
```

---

## Testing

Run all tests:

```bash
mvn test
```

---

## Author

Sonu
