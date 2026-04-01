# DOC-INT-002 Контракты обмена данными

| Версия | Статус | Дата обновления |
|---|---|---|
| v1.0 | Draft | 2026-04-01 |

## Правила контрактов

- все endpoint'ы версионируются через `/api/v1`;
- форматы DTO синхронизируются с backend кодом;
- изменения контрактов требуют обновления frontend-клиента и документации.

## Группы endpoint'ов

- `auth`: `/api/v1/auth/*` (регистрация, вход, refresh, logout, получение профиля)
- `users`: `/api/v1/users/*` (управление пользователями)
- `patients`: `/api/v1/patients/*` (операции пациента с профилем и записями)
- `doctors`: `/api/v1/doctors/*` (расписание и слоты врача)
- `appointments`: `/api/v1/appointments/*` (управление записями)
- `admins`: `/api/v1/admins/*` (администрирование)

## Базовые DTO

### Auth

```
POST /api/v1/auth/registration
{
  "login": "string",
  "fio": "string",
  "password": "string",
  "role": "patient|doctor|registrar|admin"
}
→ { "id": 123, "login": "...", "role": "..." }

POST /api/v1/auth/login
{ "login": "string", "password": "string" }
→ { "accessToken": "JWT", "refreshToken": "JWT" }

POST /api/v1/auth/refresh
{ "refreshToken": "JWT" }
→ { "accessToken": "JWT", "refreshToken": "JWT" }

GET /api/v1/auth/me
→ { "id": 123, "login": "...", "fio": "...", "role": "..." }
```

### Doctors

```
GET /api/v1/doctors?search=&specialty=&page=0&size=10
→ {
    "content": [
      { "id": 1, "fio": "Иванов И.И.", "specialty": "Терапевт" }
    ],
    "totalElements": 50,
    "totalPages": 5
  }

GET /api/v1/doctors/{doctorId}/slots?date=2026-04-01
→ {
    "doctorId": 1,
    "date": "2026-04-01",
    "slots": [
      { "slotTime": "09:00", "available": true },
      { "slotTime": "09:15", "available": false }
    ]
  }
```

### Appointments

```
POST /api/v1/appointments
{
  "doctorId": 1,
  "patientId": 2,
  "slotDateTime": "2026-04-01T09:00:00"
}
→ { "id": 101, "status": "booked", "doctorId": 1, ... }

DELETE /api/v1/appointments/{appointmentId}
→ { "id": 101, "status": "canceled" }

PATCH /api/v1/appointments/{appointmentId}
{ "newSlotDateTime": "2026-04-05T10:00:00" }
→ { "id": 101, "slotDateTime": "2026-04-05T10:00:00", ... }
```

## Контрактная дисциплина

- breaking change без мажорного пересмотра версии API не допускается;
- каждое изменение DTO сопровождается changelog-записью в docs;
- все ошибки возвращаются в единообразном формате с HTTP-кодами (400, 401, 403, 409, 500).
