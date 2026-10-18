# DOC-DAT-003 REST API спецификация

| Версия | Статус | Дата обновления |
|---|---|---|
| v1.0 | Draft | 2026-04-17 |

О документе: компактный обзор API-контуров backend RegistrationOffice без повторения полного JSON-контракта.

## Каноничный источник детальных контрактов

- `DOC-INT-002-data-exchange-contracts.md`

## Базовые правила

- базовый префикс API: `/api/v1`;
- access token передается в `Authorization: Bearer <token>`;
- refresh token передается через `HttpOnly` cookie `refresh_token`;
- JSON-поля сериализуются в `snake_case`.

## Контуры API

- `auth`: `/api/v1/auth/*`
- `users`: `/api/v1/users/*`
- `patients`: `/api/v1/patients/*`
- `doctors`: `/api/v1/doctors/*`
- `appointments`: `/api/v1/appointments/*`
- `admins`: `/api/v1/admins/*`

## Ключевые endpoint'ы

### Auth

- `POST /api/v1/auth/registration`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `GET /api/v1/auth/me`

### Users / Patients

- `GET /api/v1/users/me`
- `POST /api/v1/users/me/change-password`
- `GET /api/v1/users`
- `GET /api/v1/users/{userId}`
- `PATCH /api/v1/users/{userId}`
- `DELETE /api/v1/users/{userId}`
- `GET /api/v1/patients/me/dashboard`
- `PATCH /api/v1/patients/me/policy`
- `GET /api/v1/patients`

### Doctors / schedule

- `GET /api/v1/doctors`
- `GET /api/v1/doctors/{doctorId}/slots`
- `GET /api/v1/doctors/{doctorId}/calendar`
- `GET /api/v1/doctors/{doctorId}/schedule`
- `GET /api/v1/doctors/{doctorId}/public/slots`
- `GET /api/v1/doctors/{doctorId}/public/calendar`

### Appointments

- `POST /api/v1/appointments`
- `POST /api/v1/appointments/me`
- `GET /api/v1/appointments/{appointmentId}`
- `GET /api/v1/appointments/detail/{appointmentId}`
- `PATCH /api/v1/appointments/{appointmentId}`
- `DELETE /api/v1/appointments/{appointmentId}`

### Admin CSV

- `GET /api/v1/admins/export/doctors`
- `GET /api/v1/admins/export/patients`
- `GET /api/v1/admins/export/appointments`
- `GET /api/v1/admins/export/work-slots`
- `POST /api/v1/admins/import/doctors`
- `POST /api/v1/admins/import/patients`
- `POST /api/v1/admins/import/appointments`
- `POST /api/v1/admins/import/work-slots`

## Совместимость терминов

- в API используется `speciality` как историческое поле для врача;
- `patient_id` и `doctor_id` — это идентификаторы пользователей с соответствующей ролью;
- в старых документах встречается `patient_code` — это legacy-термин, в текущем backend не используется.

## Коды ответов

- `200`, `201` — успешно;
- `400` — ошибка валидации;
- `401` — не авторизован;
- `403` — недостаточно прав;
- `404` — не найдено;
- `409` — конфликт;
- `500` — серверная ошибка.
