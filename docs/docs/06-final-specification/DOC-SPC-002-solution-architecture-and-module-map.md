# DOC-SPC-002 Архитектура решения и карта модулей

| Версия | Статус | Дата обновления |
|---|---|---|
| v1.0 | Draft | 2026-04-01 |

## Целевой контур

- Frontend SPA
- Backend API (`/api/v1/**`)
- PostgreSQL
- Nginx reverse proxy
- Docker Compose deployment

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

## Принципы

- frontend не содержит бизнес-правила авторизации, а опирается на backend;
- role-matrix едина для UI и API;
- все интеграции идут через контрактные endpoint'ы.

## Связь с кодом

- backend: `src/main/java/ru/katacan/registrationoffice`
- конфиги: `src/main/resources/application.properties`
- infra: `docker-compose.yml`, `deploy/nginx/default.conf`
