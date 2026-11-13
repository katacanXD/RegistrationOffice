# DOC-SPC-002 Архитектура решения и карта модулей

| Версия | Статус | Дата обновления |
|---|---|---|
| v1.3 | Actual | 2026-11-07 |

## Целевой контур

- React SPA
- Spring Boot API (`/api/v1/**`)
- PostgreSQL
- Nginx reverse proxy
- Docker Compose deployment

## Карта frontend-модулей

- `AuthorizationForm`, `RegistrationForm`
- patient: `MainU`, `SearchU`, `CalendarU`
- doctor: `DoctorCalendar`
- registrar: `RegistrarPatientSearch`, `RegistrarPatientOverview`, `RegistrarDoctorSearch`, `RegistrarCalendar`
- admin: `AdminPanel`

## Карта backend-модулей

- `controller/auth`, `controller/users`, `controller/patients`, `controller/doctors`, `controller/appointments`, `controller/admins`, `controller/reports`
- `service` и `service/impl` (включая `UtilizationServiceImpl`)
- `mapper`
- `entity` (включая `AppointmentOperation`)
- `repository`
- `config`, `security`, `jwt`

## Принципы

- frontend не реализует бизнес-валидации как источник истины, а лишь даёт UX-ограничения;
- role-matrix едина для UI и API;
- контракт на `snake_case` применяется на runtime;
- refresh token хранится в `HttpOnly` cookie `refresh_token`.

## Реальные интеграционные особенности

- public calendar/slots доступны без авторизации;
- `GET /api/v1/doctors` требует авторизацию, но доступен пациенту, регистратору, администратору и врачу по своим правам;
- CSV import/export и отчёт утилизации обслуживаются только `admin`;
- отчёт утилизации — вкладка «Загрузка расписания» в админке, отдельного роута нет.

## Связь с кодом

- frontend: `frontend/src`
- backend: `backend/src/main/java/ru/katacan/registrationoffice`
- конфиги: `backend/src/main/resources/application.properties`
- infra: `docker-compose.yml`, `deploy/nginx/default.conf`
