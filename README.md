# TaskFlow

Backend-сервис для управления проектами, задачами и командной работой на Java 21 и Spring Boot 4.1.1.

## Возможности

- REST API для регистрации, проектов, участников, задач, меток, комментариев и уведомлений.
- Аутентификация и авторизация на основе JWT и Spring Security.
- PostgreSQL и версионируемые миграции Flyway.
- История изменений задач и оптимистическая блокировка.
- WebSocket/STOMP для событий задач.
- Фоновые уведомления о приближающихся дедлайнах.
- Документация API через OpenAPI и Swagger UI.
- Тесты на JUnit 5 и PostgreSQL в Testcontainers.

## Требования

- Java 21
- Docker Desktop с работающим Docker Engine (для Docker-запуска и интеграционных тестов)

## Запуск всего приложения через Docker

Из корня проекта:

```powershell
docker compose up --build
```

Compose сначала дождётся готовности PostgreSQL, затем соберёт и запустит приложение. После старта:

- API: <http://localhost:8080>
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>
- PostgreSQL с хоста: `localhost:5434` (база `taskflow_db`, пользователь и пароль `postgres`)

Остановить приложение:

```powershell
docker compose down
```

Чтобы также удалить данные PostgreSQL:

```powershell
docker compose down -v
```

Для локальной разработки можно поднять только базу и запустить приложение через Gradle:

```powershell
docker compose up -d postgres
.\gradlew.bat bootRun
```

Локальный backend использует `jdbc:postgresql://localhost:5434/taskflow_db`. Имя пользователя и пароль по умолчанию — `postgres`; их можно переопределить переменными окружения `DB_USERNAME` и `DB_PASSWORD`.

## Быстрая проверка API

Создайте пользователя:

```powershell
curl.exe -X POST http://localhost:8080/api/auth/register `
  -H "Content-Type: application/json" `
  -d "{\"username\":\"alice\",\"email\":\"alice@example.com\",\"password\":\"secret123\",\"fullName\":\"Alice\"}"
```

В ответе регистрации или входа вернётся JWT-токен. Передавайте его для защищённых запросов:

```text
Authorization: Bearer <token>
```

Например, создать проект:

```powershell
curl.exe -X POST http://localhost:8080/api/projects `
  -H "Authorization: Bearer <token>" `
  -H "Content-Type: application/json" `
  -d "{\"name\":\"Мой проект\",\"description\":\"Описание проекта\"}"
```

Регистрация и вход доступны без токена: `POST /api/auth/register` и `POST /api/auth/login`. Остальные API-запросы требуют аутентификации.

## Тесты

Запустить тесты:

```powershell
.\gradlew.bat test
```

Интеграционные тесты поднимают PostgreSQL через Testcontainers и поэтому требуют работающий Docker Engine.

## Конфигурация

Основные параметры можно переопределить переменными окружения:

| Переменная | По умолчанию | Назначение |
| --- | --- | --- |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5434/taskflow_db` | JDBC URL базы данных |
| `DB_USERNAME` | `postgres` | Пользователь PostgreSQL при локальном запуске |
| `DB_PASSWORD` | `postgres` | Пароль PostgreSQL при локальном запуске |
| `JWT_SECRET` | локальный ключ из конфигурации | Секрет подписи JWT |

Для Compose можно задать `JWT_SECRET` в окружении перед запуском. Не используйте ключ по умолчанию в production.
