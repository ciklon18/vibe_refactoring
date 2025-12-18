# Java Application (ShopAPI)

Spring Boot 17 backend for a small electronics store. The service exposes CRUD endpoints for laptops, monitors, personal computers, and hard drives plus an aggregated analytics endpoint.

## Architecture

* **Frameworks**: Spring Boot, Spring Data JPA, OpenAPI annotations (Swagger UI at `/swagger-ui/index.html`).
* **Persistence**: PostgreSQL via `application.properties` configuration. Entities live under `src/main/java/testtask/shift/shopapi/model/**`.
* **API layers**:
  * Controllers under `controller/` expose REST endpoints.
  * Services under `service/` own business logic; repositories extend `CrudRepository` for persistence.
  * DTOs such as `StatsResponse` live under `model/analytics`.
* **Analytics**: `StatsServiceImpl` walks repository data once per category to compute counts and stock totals, ensuring null stock values are treated as zero.

## Running locally

Prerequisites: JDK 17, Maven, and a reachable PostgreSQL instance configured in [`src/main/resources/application.properties`](src/main/resources/application.properties).

```sh
mvn clean install
mvn spring-boot:run
```

Build a container image with the included Dockerfile (from repo root):

```sh
docker build -t shopapi:local -f java-app/Dockerfile java-app
docker run -p 8080:8080 shopapi:local
```

## API overview

* `GET /api/laptops` (list), `GET /api/laptops/{id}`, `POST /api/laptops/add`, `PUT /api/laptops/{id}`
* Similar CRUD routes exist for `/api/monitors`, `/api/personal-computers`, and `/api/hard-drives`.
* `GET /api/stats` — aggregated counts and total stock units for all categories.
* `GET /api/stats/insights` — per-category counts, stock, average price, and inventory value.

## Testing

Run the unit and MVC slice tests:

```sh
mvn test
```

If your environment cannot download Maven Central artifacts (e.g., HTTP 403 from the parent POM), retry from a networked environment or with a configured proxy/cache.

## Load testing

### k6

A lightweight k6 scenario targets `/api/laptops`:

```sh
BASE_URL=http://localhost:8080 k6 run load-tests/k6-shop.js
```

The script aims for P95 latency under 500 ms and <1% error rate.

### Stdlib Python runner

A dependency-free runner stresses the same endpoint and reports RPS/latency/error metrics. Use the bundled mock server when the app is not running:

```sh
python load-tests/run_load_test.py --mock --duration 5 --concurrency 20
```

To hit a live service, remove `--mock` and adjust `--url`.

## CI

GitHub Actions workflow `.github/workflows/maven.yml` builds and tests the project on each push/PR.
