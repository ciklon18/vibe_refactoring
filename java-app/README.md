# Java Application (ShopAPI)

Spring Boot 17 приложение для небольшого магазина электроники. Сервис предоставляет CRUD-эндпоинты для ноутбуков, мониторов, персональных компьютеров и жёстких дисков, а также агрегированную аналитику по складу.

## Архитектура

- **Фреймворк**: Spring Boot, Spring Web, Spring Data JPA.
- **Документация API**: OpenAPI аннотации, Swagger UI доступен по `/swagger-ui/index.html`.
- **База данных**: PostgreSQL. Настройки находятся в `src/main/resources/application.properties`.
- **Слои приложения**:
  - `controller/` — REST-контроллеры.
  - `service/` — бизнес-логика и агрегации.
  - `repository/` — интерфейсы Spring Data `CrudRepository`.
  - `model/` — сущности и DTO, в том числе аналитика в `model/analytics`.
- **Аналитика**: `StatsServiceImpl` вычисляет количество и остатки по всем категориям, обрабатывая `null` в поле stock как ноль.

## Полный процесс выполнения пунктов

### 1) Подготовка окружения
- Требования: JDK 17, доступ к PostgreSQL.
- Укажите настройки БД в `src/main/resources/application.properties` или передайте через переменные окружения `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`.

### 2) Запуск приложения локально (Maven)
```sh
./mvnw spring-boot:run
```
Приложение поднимется на `http://localhost:8080`.

### 3) Запуск в Docker
Сборка и запуск контейнера из корня репозитория:
```sh
docker build -t shopapi:local -f java-app/Dockerfile java-app
docker run -p 8080:8080 --env-file java-app/.env.example shopapi:local
```
Файл `.env.example` можно скопировать в `.env` и заполнить своими параметрами подключения к БД.

### 4) Юнит- и интеграционные тесты
```sh
./mvnw test
```
Если загрузка зависимостей из внешней сети недоступна, выполните команду в среде с доступом к Maven Central или используйте локальный кэш/прокси Maven.

### 5) Нагрузочное тестирование
- **Быстрый прогон на стандартной библиотеке**
  ```sh
  python load-tests/run_load_test.py --mock --duration 5 --concurrency 20
  ```
  Ключ `--mock` поднимает встроенный HTTP-сервер (порт 18080), чтобы оценить RPS/latency/error rate без внешних зависимостей. Для реального сервиса уберите `--mock` и передайте `--url` при необходимости.

- **Пример с `k6`**
  Простейший сценарий можно выполнить любым HTTP-генератором трафика (например, `k6`, `hey`, `ab`). Пример для `k6` (save as `load.js` и запустите `k6 run load.js`):
  ```js
  import http from 'k6/http';
  import { check, sleep } from 'k6';

export let options = { vus: 10, duration: '30s' };

export default function () {
  const res = http.get('http://localhost:8080/api/stats');
  check(res, { 'status is 200': (r) => r.status === 200 });
  sleep(1);
}
  ```
  Метрики RPS, latency и error rate выводятся в консоль `k6`.

### 6) Проверка аналитических эндпоинтов
```sh
curl http://localhost:8080/api/stats
curl http://localhost:8080/api/stats/insights
```
Оба ответа содержат агрегированные данные по категориям: количество, остатки, среднюю цену и общую стоимость.

### 7) CI/CD пайплайн
Workflow `.github/workflows/maven.yml` использует Maven: выполняет `./mvnw verify`, а затем собирает Docker-образ. Запускается на push/PR, позволяя автоматически проверять сборку и контейнер.

## API

- `GET /api/laptops`, `GET /api/laptops/{id}`, `POST /api/laptops/add`, `PUT /api/laptops/{id}`
- Аналогичные CRUD-методы для `/api/monitors`, `/api/personal-computers`, `/api/hard-drives`.
- `GET /api/stats` — суммарное количество товаров и остатки по категориям.
- `GET /api/stats/insights` — количество, остатки, средняя цена и общая стоимость по категориям.

## Тестирование

Запуск юнит- и MVC-тестов:
```sh
./mvnw test
```
Если загрузка зависимостей из внешней сети недоступна (например, HTTP 403 при скачивании parent POM), выполните команду в среде с доступом в интернет или используйте локальный прокси/кэш Maven.
