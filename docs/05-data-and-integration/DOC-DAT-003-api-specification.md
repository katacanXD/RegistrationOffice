# DOC-DAT-003 — REST API спецификация

| Версия | Статус | Дата создания | Дата обновления |
|--------|--------|---------------|-----------------|
| v0.2   | Draft  | 2026-04-10    | 2026-04-10      |

О документе: описание REST API эндпоинтов системы RegistrationOffice.

Для кого: frontend-разработчик, backend-разработчик, тестировщик.

Основано на: [ERD](DOC-DAT-001-erd.md), [user stories](../02-requirements/DOC-REQ-001-user-stories.md), контроллеры в `src/main/java/ru/katacan/registrationoffice/controller`.

---

## Авторы документа

- Чернышов Ярослав — backend developer — разработка API
- Лукьянов Илья — frontend developer — валидация контрактов

## История изменений

- [v0.1] [2026-04-10] (Чернышов): первичная спецификация.
- [v0.2] [2026-04-10] (Чернышов, Лукьянов): добавлены эндпоинты аутентификации.

---

## Базовый URL

```
http://registration-office.ru/api
```

## Аутентификация

Все эндпоинты (кроме `/api/auth/**`) требуют JWT-токен в заголовке:

```
Authorization: Bearer <access_token>
```

---

## Auth — Аутентификация

### POST /api/auth/register

Регистрация нового пользователя.

| Параметр       | Тип    | Обязательный | Описание            |
|----------------|--------|--------------|---------------------|
| username       | string | ✓            | Логин               |
| password       | string | ✓            | Пароль              |
| role           | string | ✓            | PATIENT / DOCTOR / REGISTRAR / ADMIN |

Ответ: `201 Created` — `{ "user_id", "username", "role" }`

### POST /api/auth/login

Вход в систему.

| Параметр  | Тип    | Обязательный | Описание |
|-----------|--------|--------------|----------|
| username  | string | ✓            | Логин    |
| password  | string | ✓            | Пароль   |

Ответ: `200 OK` — `{ "access_token", "refresh_token", "token_type": "Bearer" }`

### POST /api/auth/refresh

Обновление access-токена.

| Параметр       | Тип    | Обязательный | Описание       |
|----------------|--------|--------------|----------------|
| refresh_token  | string | ✓            | Refresh-токен  |

Ответ: `200 OK` — `{ "access_token", "refresh_token" }`

---

## Doctors — Врачи

### GET /api/doctors

Получить список всех врачей. Доступ: все авторизованные.

Ответ: `200 OK` — массив `{ doctor_id, fio, specialty }`

### GET /api/doctors/{id}

Получить врача по ID.

Ответ: `200 OK` — `{ doctor_id, fio, specialty }`
Ошибки: `404 Not Found`

### POST /api/doctors

Создать врача. Доступ: ADMIN.

| Параметр  | Тип    | Обязательный | Описание        |
|-----------|--------|--------------|-----------------|
| fio       | string | ✓            | ФИО врача       |
| specialty | string | ✓            | Специальность   |

Ответ: `201 Created`

---

## Patients — Пациенты

### GET /api/patients

Получить список пациентов. Доступ: REGISTRAR, ADMIN.

### GET /api/patients/{code}

Получить пациента по коду.

### POST /api/patients

Создать пациента. Доступ: REGISTRAR, ADMIN.

| Параметр    | Тип     | Обязательный | Описание       |
|-------------|---------|--------------|----------------|
| fio         | string  | ✓            | ФИО            |
| birth_year  | integer |              | Год рождения   |

---

## Work Slots — Рабочие интервалы

### GET /api/work-slots?doctorId={id}&date={date}

Получить рабочие слоты врача на дату. Доступ: все авторизованные.

Параметры запроса:
- `doctorId` (обязательный) — ID врача
- `date` (обязательный) — дата в формате `YYYY-MM-DD`

Ответ: `200 OK` — массив `{ slot_id, doctor_id, date, start_time, end_time, slot_minutes, break_start, break_end }`

### POST /api/work-slots

Создать рабочий интервал. Доступ: ADMIN.

| Параметр     | Тип     | Обязательный | Описание              |
|--------------|---------|--------------|---------------------- |
| doctor_id    | long    | ✓            | ID врача              |
| date         | string  | ✓            | Дата (YYYY-MM-DD)     |
| start_time   | string  | ✓            | Начало (HH:MM)        |
| end_time     | string  | ✓            | Конец (HH:MM)         |
| slot_minutes | integer | ✓            | Длительность слота    |
| break_start  | string  |              | Начало перерыва       |
| break_end    | string  |              | Конец перерыва        |

Ответ: `201 Created`
Ошибки: `400 Bad Request` (валидация), `404 Not Found` (врач не найден)

### POST /api/work-slots/import-csv

Импорт рабочих слотов из CSV. Доступ: ADMIN.

Тело: `multipart/form-data`, файл CSV.
Ответ: `200 OK` — `{ "imported": N, "skipped": M }`

---

## Appointments — Записи на приём

### GET /api/appointments?doctorId={id}&date={date}&status={status}

Получить записи. Доступ: зависит от роли.

Параметры запроса (все опциональные):
- `doctorId` — фильтр по врачу
- `date` — фильтр по дате
- `status` — фильтр по статусу (booked/canceled/completed)

### GET /api/appointments/{id}

Получить запись по ID.

### POST /api/appointments

Создать запись (бронирование). Доступ: PATIENT (свою), REGISTRAR (любую).

| Параметр       | Тип    | Обязательный | Описание              |
|----------------|--------|--------------|---------------------- |
| doctor_id      | long   | ✓            | ID врача              |
| slot_datetime  | string | ✓            | Дата-время слота      |
| patient_code   | long   | ✓            | Код пациента          |

Ответ: `201 Created`
Ошибки: `400 Bad Request` (валидация — двойное бронирование, слот вне интервала, перерыв), `404 Not Found`

### PUT /api/appointments/{id}/cancel

Отменить запись. Доступ: PATIENT (свою), REGISTRAR (любую).

| Параметр       | Тип    | Обязательный | Описание         |
|----------------|--------|--------------|------------------|
| cancel_reason  | string |              | Причина отмены   |

Ответ: `200 OK`
Ошибки: `404 Not Found`, `400 Bad Request` (уже отменена)

### PUT /api/appointments/{id}/reschedule

Перенести запись. Доступ: PATIENT (свою), REGISTRAR (любую).

| Параметр           | Тип    | Обязательный | Описание                  |
|--------------------|--------|--------------|---------------------------|
| new_slot_datetime  | string | ✓            | Новые дата-время слота    |

Ответ: `200 OK` — `{ "old_appt_id", "new_appt_id" }`

### PUT /api/appointments/{id}/complete

Завершить приём. Доступ: DOCTOR, REGISTRAR.

Ответ: `200 OK`
Ошибки: `400 Bad Request` (slot_datetime ещё не наступил)

---

## Reports — Отчёты

### GET /api/reports/utilization?from={date}&to={date}&doctorId={id}&specialty={spec}

Отчёт утилизации. Доступ: ADMIN.

Параметры запроса:
- `from` (обязательный) — начало периода
- `to` (обязательный) — конец периода
- `doctorId` — фильтр по врачу
- `specialty` — фильтр по специальности

Ответ: `200 OK`

```json
[
  {
    "doctor_id": 1,
    "fio": "Смирнова Е.В.",
    "date": "2026-04-10",
    "slots_total": 16,
    "slots_booked": 12,
    "slots_canceled": 2,
    "utilization": 0.75
  }
]
```

### GET /api/reports/utilization/export-csv?from={date}&to={date}

Экспорт отчёта в CSV. Доступ: ADMIN.

Ответ: `200 OK`, Content-Type: `text/csv`
Формат: `doctor_id,date,slots_total,slots_booked,utilization`

---

## Коды ответов (общие)

| Код | Описание                                           |
|-----|-----------------------------------------------------|
| 200 | Успешный запрос                                     |
| 201 | Ресурс создан                                       |
| 400 | Ошибка валидации (описание в теле ответа)           |
| 401 | Не авторизован (отсутствует или невалидный токен)    |
| 403 | Доступ запрещён (недостаточно прав)                  |
| 404 | Ресурс не найден                                    |
| 409 | Конфликт (например, двойное бронирование)            |
| 500 | Внутренняя ошибка сервера                            |
