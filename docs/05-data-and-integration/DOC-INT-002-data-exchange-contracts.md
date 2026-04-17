# DOC-INT-002 Контракты обмена данными

| Версия | Статус | Дата обновления |
|---|---|---|
| v1.1 | Draft | 2026-04-17 |

## Правила контрактов

- все endpoint'ы версионируются через `/api/v1`;
- JSON-поля в запросах и ответах сериализуются в `snake_case`;
- refresh token передается через `HttpOnly` cookie `refresh_token`, а не в теле JSON;
- изменения контрактов требуют синхронизации frontend-клиента и документации.

## Группы endpoint'ов

- `auth`: `/api/v1/auth/*` — регистрация, вход, refresh, logout, профиль;
- `users`: `/api/v1/users/*` — профиль пользователя и админский CRUD;
- `patients`: `/api/v1/patients/*` — кабинет пациента и список пациентов;
- `doctors`: `/api/v1/doctors/*` — поиск врачей, слоты, календарь и расписание;
- `appointments`: `/api/v1/appointments/*` — создание, просмотр, изменение и отмена записей;
- `admins`: `/api/v1/admins/*` — CSV export/import.

## Аутентификация и профиль

```http
POST /api/v1/auth/registration
{
  "login": "patient_001",
  "fio": "Иванов Иван Иванович",
  "password": "pass12345"
}
→ 200 OK
{
  "user_id": 123,
  "fio": "Иванов Иван Иванович",
  "role": "patient",
  "message": "Пользователь успешно зарегистрирован"
}

POST /api/v1/auth/login
{
  "login": "patient_001",
  "password": "pass12345"
}
→ 200 OK
Set-Cookie: refresh_token=...; HttpOnly; SameSite=Lax; Secure
{
  "access_token": "JWT",
  "user": {
    "user_id": 123,
    "fio": "Иванов Иван Иванович",
    "role": "patient",
    "policy_number": null,
    "specialty": null
  }
}

POST /api/v1/auth/refresh
Cookie: refresh_token=...
→ 200 OK
Set-Cookie: refresh_token=...; HttpOnly; SameSite=Lax; Secure
{
  "access_token": "JWT",
  "user": { "user_id": 123, "fio": "...", "role": "...", "policy_number": null, "specialty": null }
}

POST /api/v1/auth/logout
Cookie: refresh_token=...
→ 200 OK
Set-Cookie: refresh_token=; Max-Age=0; HttpOnly; SameSite=Lax; Secure
"Пользователь успешно вышел из системы"

GET /api/v1/auth/me
Authorization: Bearer <access_token>
→ 200 OK
{
  "user_id": 123,
  "fio": "Иванов Иван Иванович",
  "role": "patient",
  "policy_number": "1234567890123456",
  "specialty": null
}
```

## Пользователи и пациенты

```http
GET /api/v1/users/me
Authorization: Bearer <access_token>
→ 200 OK
{ "user_id": 123, "fio": "...", "role": "...", "policy_number": null, "specialty": null }

POST /api/v1/users/me/change-password
Authorization: Bearer <access_token>
{
  "old_password": "pass12345",
  "new_password": "newPass123"
}
→ 200 OK
{ "success": true, "message": "Пароль успешно изменен" }

GET /api/v1/users?search=Иванов&page=0&size=10
Authorization: Bearer <access_token>
→ 200 OK
{
  "users": [
    { "user_id": 123, "fio": "Иванов Иван Иванович", "role": "patient", "policy_number": null, "specialty": null }
  ],
  "total_found": 1,
  "total_elements": 1,
  "total_pages": 1,
  "current_page": 0
}

GET /api/v1/users/{user_id}
Authorization: Bearer <access_token>
→ 200 OK
{ "user_id": 123, "fio": "...", "role": "...", "policy_number": null, "specialty": null }

PATCH /api/v1/users/{user_id}
Authorization: Bearer <access_token>
{
  "policy_number": "1234567890123456",
  "speciality": "Терапевт",
  "role": "doctor"
}
→ 200 OK
{ "fio": "...", "policy_number": null, "speciality": "Терапевт", "role": "doctor" }

DELETE /api/v1/users/{user_id}
Authorization: Bearer <access_token>
→ 200 OK
{ "success": true, "message": "Пользователь успешно удален" }

GET /api/v1/patients/me/dashboard
Authorization: Bearer <access_token>
→ 200 OK
{
  "user": { "fio": "...", "policy_number": "1234567890123456", "is_policy_verified": true },
  "appointments": []
}

PATCH /api/v1/patients/me/policy
Authorization: Bearer <access_token>
{
  "policy_number": "1234567890123456"
}
→ 200 OK
{ "success": true, "message": "Полис успешно привязан. Теперь вы можете записываться на прием." }

GET /api/v1/patients?search=&page=0&size=10
Authorization: Bearer <access_token>
→ 200 OK
{
  "patients": [
    { "user_id": 123, "fio": "...", "policy_number": "1234567890123456" }
  ],
  "total_found": 1,
  "total_elements": 1,
  "total_pages": 1,
  "current_page": 0
}
```

## Врачи и расписание

```http
GET /api/v1/doctors?search=терап&page=0&size=10
Authorization: Bearer <access_token>
→ 200 OK
{
  "doctors": [
    { "doctor_id": 10, "fio": "Врач Тестовый 001", "speciality": "Терапевт" }
  ],
  "total_found": 1,
  "total_elements": 1,
  "total_pages": 1,
  "current_page": 0
}

GET /api/v1/doctors/{doctor_id}/slots?date=2026-04-01
Authorization: Bearer <access_token>
→ 200 OK
{
  "doctor_id": 10,
  "date": "2026-04-01",
  "slots": [
    { "time": "09:00", "is_available": true }
  ],
  "break_": { "start": "13:00", "end": "14:00" }
}

GET /api/v1/doctors/{doctor_id}/calendar?month=4&year=2026
Authorization: Bearer <access_token>
→ 200 OK
{
  "work_days": [
    { "date": "2026-04-01", "has_appointments": true, "total_slots": 8, "closed_slots": 2 }
  ]
}

GET /api/v1/doctors/{doctor_id}/schedule?date=2026-04-01
Authorization: Bearer <access_token>
→ 200 OK
{
  "date": "2026-04-01",
  "slots": [
    { "time": "09:00", "type": "open", "appointment_id": null }
  ],
  "break_": { "start": "13:00", "end": "14:00" }
}

GET /api/v1/doctors/{doctor_id}/public/slots?date=2026-04-01
GET /api/v1/doctors/{doctor_id}/public/calendar?month=4&year=2026
→ public endpoints для просмотра без авторизации
```

## Записи на прием

```http
POST /api/v1/appointments
Authorization: Bearer <access_token>
{
  "doctor_id": 10,
  "patient_id": 123,
  "slot_datetime": "2026-04-01T09:00:00"
}
→ 201 Created
{
  "appointment_id": 501,
  "message": "Запись успешно создана на 1 апреля, 09:00",
  "details": {
    "doctor_fio": "Врач Тестовый 001",
    "speciality": "Терапевт",
    "address": "г. Москва, ул. Менделеева, д. 15с2"
  }
}

POST /api/v1/appointments/me
Authorization: Bearer <access_token>
{
  "doctor_id": 10,
  "slot_datetime": "2026-04-01T09:00:00"
}
→ 201 Created
{ ...создана запись для текущего пациента... }

GET /api/v1/appointments/{appointment_id}
Authorization: Bearer <access_token>
→ 200 OK
{
  "appointment_id": 501,
  "speciality": "Терапевт",
  "doctor_fio": "Врач Тестовый 001",
  "datetime": "2026-04-01T09:00:00",
  "address": "г. Москва, ул. Менделеева, д. 15с2",
  "status": "booked"
}

GET /api/v1/appointments/detail/{appointment_id}
Authorization: Bearer <access_token>
→ 200 OK
{
  "appointment_id": 501,
  "datetime": "2026-04-01T09:00:00",
  "doctor": { "fio": "Врач Тестовый 001" },
  "patient": { "fio": "Иванов Иван Иванович", "policy_number": "1234567890123456" },
  "address": "г. Москва, ул. Менделеева, д. 15с2"
}

PATCH /api/v1/appointments/{appointment_id}
Authorization: Bearer <access_token>
{
  "doctor_id": 10,
  "slot_datetime": "2026-04-01T10:00:00",
  "status": "booked"
}
→ 200 OK
{ "doctor_id": 10, "slot_datetime": "2026-04-01 10:00", "status": "booked", "message": "..." }

DELETE /api/v1/appointments/{appointment_id}
Authorization: Bearer <access_token>
→ 200 OK
{
  "appointment_id": 501,
  "status": "canceled",
  "message": "Запись к врачу (Терапевт) отменена"
}
```

## Администрирование CSV

```http
GET /api/v1/admins/export/doctors
GET /api/v1/admins/export/patients
GET /api/v1/admins/export/appointments
GET /api/v1/admins/export/work-slots
→ 200 OK
{
  "file_name": "doctors.csv",
  "rows_exported": 10,
  "timestamp": "2026-04-17T12:00:00",
  "status": "SUCCESS",
  "message": "Успешно экспортировано врачей: 10",
  "csv_content": "login,fullName,speciality\n..."
}

POST /api/v1/admins/import/doctors
Content-Type: multipart/form-data
file=<csv>

POST /api/v1/admins/import/patients
POST /api/v1/admins/import/appointments
POST /api/v1/admins/import/work-slots
→ 200 OK / 400 Bad Request
{
  "rows_processed": 10,
  "rows_successful": 9,
  "rows_failed": 1,
  "errors": [
    { "row_number": 4, "error_message": "...", "data": "..." }
  ],
  "timestamp": "2026-04-17T12:00:00",
  "status": "PARTIAL",
  "message": "Обработано: 10, успешно: 9, ошибок: 1"
}
```

## Контрактная дисциплина

- breaking change без пересмотра версии API не допускается;
- каждое изменение DTO сопровождается обновлением docs и frontend-контрактов;
- ошибки возвращаются через стандартные HTTP-коды домена: `400`, `401`, `403`, `404`, `409`, `500`.
