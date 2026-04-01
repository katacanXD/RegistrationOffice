# RegistrationOffice

Backend API для медицинской регистратуры: пользователи, роли, расписание врачей и записи на прием.

## Что есть в проекте

- JWT-аутентификация (`access` + `refresh`) и роли: `admin`, `registrar`, `doctor`, `patient`
- Управление пользователями и профилем (`/api/v1/users`, `/api/v1/auth/me`)
- Список врачей, календарь и слоты (`/api/v1/doctors/**`)
- Записи на прием (`/api/v1/appointments/**`)
- Инициализация тестовых ролей/статусов/пользователей/слотов при первом запуске (`DataInitializer`)

## Технологии

- Java 21
- Spring Boot 4.0.3
- Spring Data JPA, Spring Security, Validation
- PostgreSQL 16
- JWT (`io.jsonwebtoken:jjwt`)
- Docker, Docker Compose, Nginx
- Gradle 9.3.1

## API (базовые префиксы)

- `POST /api/v1/auth/registration`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `GET /api/v1/auth/me`
- `GET /api/v1/users/**`
- `GET /api/v1/patients/**`
- `GET /api/v1/doctors/**`
- `GET/POST/PATCH/DELETE /api/v1/appointments/**`

Актуальные маппинги: `src/main/java/ru/katacan/registrationoffice/controller`.

## Быстрый старт (Docker Compose)

### 1) Подготовить `.env`

Скопируйте пример:

```powershell
Copy-Item .env.example .env
```

Минимально заполните:

```dotenv
DB_NAME=postgres
DB_USER=postgres
DB_PASS=admin
SECURITY_JWT_SECRET=change_me_super_long_random_secret_min_32_chars
APP_SECURITY_CORS_ALLOWED_ORIGINS=
APP_SECURITY_CORS_ALLOWED_ORIGIN_PATTERNS=
```

### 2) Поднять сервисы

```powershell
docker compose up -d --build
```

### 3) Проверить, что все запущено

```powershell
docker compose ps
docker compose logs --tail 100 backend
```

После старта API доступно через Nginx на `http://localhost`.

## Локальный запуск без Docker

Нужно вручную поднять PostgreSQL и передать переменные окружения (`DB_NAME`, `DB_USER`, `DB_PASS`, `SECURITY_JWT_SECRET`).

```powershell
.\gradlew.bat bootRun
```

## Переменные окружения

Основные переменные описаны в `.env.example`:

- `DB_NAME`, `DB_USER`, `DB_PASS`
- `SECURITY_JWT_SECRET`
- `APP_SECURITY_CORS_ALLOWED_ORIGINS`
- `APP_SECURITY_CORS_ALLOWED_ORIGIN_PATTERNS`

См. также `src/main/resources/application.properties`.

## Документация

- Общие проектные заметки: `HELP.md`
- Инструкция по деплою в Docker на VM: `DEPLOY_YC_DOCKER.md`
- Карта папки `docs`: `docs/README.md`

## Структура проекта

- Код backend: `src/main/java/ru/katacan/registrationoffice`
- Конфиги приложения: `src/main/resources`
- Docker/Nginx: `Dockerfile`, `docker-compose.yml`, `deploy/nginx/default.conf`
- Документация: `docs/`
