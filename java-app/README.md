# Java Application (ShopAPI)

## Description

Backend test task for SHIFT Lab 2022.
Simple API for an electronics store powered by Spring Boot + PostgreSQL.

## Specification
Swagger specification is available here:
```
http://localhost:8080/swagger-ui/index.html
```

## Installation

**Using Maven plugin**

First you should do clean installation:
```sh
$ mvn clean install
```
You can start application using Spring Boot custom command:
```sh
$ mvn spring-boot:run
```

## Testing

Run the test suite (unit and MVC slice tests):

```sh
$ mvn test
```

## Analytics

An aggregated statistics endpoint is available at `GET /api/stats` returning:

- total products across all categories
- counts for laptops, monitors, personal computers and hard drives
- total stock units available across the catalog

## Load testing (k6)

The `load-tests/k6-shop.js` script drives lightweight traffic against `/api/laptops`.

```sh
$ BASE_URL=http://localhost:8080 k6 run load-tests/k6-shop.js
```

The script targets <500 ms P95 latency and <1% error rate by default.

## CI

GitHub Actions workflow `.github/workflows/maven.yml` builds the project and runs tests on every push/PR so that analytics and catalog endpoints stay healthy.

## Configuration

To configure the application, use <a href="src/main/resources/application.properties">application.properties</a>
