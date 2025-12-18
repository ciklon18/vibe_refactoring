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

## Запуск

### 1. Локально (Maven)
Требования: JDK 17, Maven, доступный PostgreSQL.
1. Настройте подключение в `src/main/resources/application.properties` (URL, пользователь, пароль).
2. Выполните сборку и запуск:
```sh
mvn clean install
mvn spring-boot:run
```

### 2. В Docker
Сборка и запуск контейнера из корня репозитория:
```sh
docker build -t shopapi:local -f java-app/Dockerfile java-app
docker run -p 8080:8080 --env-file java-app/.env.example shopapi:local
```
При необходимости создайте `.env` или пробросьте переменные окружения `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`.

## API

- `GET /api/laptops`, `GET /api/laptops/{id}`, `POST /api/laptops/add`, `PUT /api/laptops/{id}`
- Аналогичные CRUD-методы для `/api/monitors`, `/api/personal-computers`, `/api/hard-drives`.
- `GET /api/stats` — суммарное количество товаров и остатки по категориям.
- `GET /api/stats/insights` — количество, остатки, средняя цена и общая стоимость по категориям.

## Тестирование

Запуск юнит- и MVC-тестов:
```sh
mvn test
```
Если загрузка зависимостей Maven из внешней сети недоступна (например, HTTP 403 при скачивании parent POM), выполните команду в среде с доступом в интернет или используйте локальный прокси/кэш.

## CI/CD

Workflow `.github/workflows/maven.yml` собирает проект и выполняет тесты при каждом push/PR, а затем строит Docker-образ для контроля сборки.
