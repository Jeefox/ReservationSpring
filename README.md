# Reservation System

REST API системы бронирования комнат со статусной машиной, event-driven уведомлениями и infrastructure-as-code.

## 🛠 Стек
- **Java 21** (record'ы, switch expressions, text blocks)
- **Spring Boot 4** + Spring Data JPA + Hibernate 7
- **PostgreSQL 16** + **Flyway** (миграции как код в git)
- **Docker Compose** (окружение одной командой)
- **JUnit 5** + **Mockito** + **SpringDoc OpenAPI 3**

## 🏗 Архитектурные решения
- **Event-driven** через `ApplicationEventPublisher` + `@TransactionalEventListener(AFTER_COMMIT)`
  — сервис не знает о уведомлениях, слабая связанность
- **Статусная машина** — правила FSM инкапсулированы в enum, PATCH-эндпоинт `/reservations/{id}/status`
- **Единый контракт ошибок** `ApiError` для всех 4xx/5xx
- **DTO-границы** на всех ресурсах (схемы входа и выхода изолированы от Entity)
- **Профили Spring**: `dev` (H2 + Swagger + сидер) и `prod` (PostgreSQL + Flyway, без Swagger)

## 🚀 Запуск

### Production (Docker + PostgreSQL + Flyway)
```bash
docker compose up -d
mvn spring-boot:run -Dspring-boot.run.profiles=prod