# DOC-SPC-002 Архитектура решения и карта модулей

| Версия | Статус | Дата обновления |
|---|---|---|
| v1.1 | Draft | 2026-04-17 |

## Целевой контур

- Frontend SPA
- Backend API (`/api/v1/**`)
- PostgreSQL
- Nginx reverse proxy
- Docker Compose deployment

## Состав backend-контуров

- `controller/auth`, `controller/users`, `controller/patients`, `controller/doctors`, `controller/appointments`, `controller/admins`
- `service` и `service/impl` для бизнес-логики
- `mapper` для DTO-ответов
- `config` для security, CORS и инициализации тестовых данных

## Карта модулей frontend

- auth
- patient-cabinet
- doctor-cabinet
- registrar-workplace
- admin-users

## Карта модулей backend

- `controller`
- `service`
- `repository`
- `security/jwt`
- `entity`
- `mapper`
- `config`

## Принципы

- frontend не содержит бизнес-правила авторизации, а опирается на backend;
- role-matrix едина для UI и API;
- все интеграции идут через контрактные endpoint'ы.

## Реальные интеграционные особенности

- refresh token живет в `HttpOnly` cookie `refresh_token`;
- public calendar/slots доступны для просмотра без авторизации;
- CSV export/import обслуживается только `admin`-ролью;
- `GET /api/v1/doctors` требует аутентификацию, но не требует админской роли.

## Связь с кодом

- backend: `backend/src/main/java/ru/katacan/registrationoffice`
- конфиги: `backend/src/main/resources/application.properties`
- infra: `docker-compose.yml`, `deploy/nginx/default.conf`
