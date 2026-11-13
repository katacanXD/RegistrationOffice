# DOC-DAT-005 — Таблица маппинга данных

| Версия | Статус | Дата создания | Дата обновления |
|--------|--------|---------------|-----------------|
| v0.3   | Actual | 2026-04-14    | 2026-11-07      |

О документе: маппинг CSV-обмена и ключевых API-контуров RegistrationOffice на фактическую модель `t_users`, `t_work_slots`, `t_appointments`, `t_appointment_operations`, `t_refresh_tokens`.

Для кого: backend-разработчик, аналитик.

Основано на: [ERD](DOC-DAT-001-logical-data-model-and-erd.md), [API](DOC-DAT-003-api-specification.md), [DFD](DOC-DAT-004-dfd.md).

---

## Авторы документа

- Наботов Карим — project lead
- Чернышов Ярослав — backend developer

## История изменений

- [v0.1] [2026-04-14] (Наботов): первичный маппинг.
- [v0.2] [2026-04-18] (Чернышов): добавлены маппинги для отчёта утилизации и API.
- [v0.3] [2026-11-07] (Чернышов): приведено к фактическим эндпоинтам и таблицам; дубли CSV не пропускаются молча, а возвращаются в отчёте импорта.

---

## CSV → врачи (`t_users`, роль `doctor`)

Эндпоинт: `POST /api/v1/admins/import/doctors`.

| Поле CSV | Поле БД | Преобразование |
|---|---|---|
| `login` | `t_users.f_login` | нормализация логина; проверка уникальности |
| `fullName` | `t_users.f_fio` | прямое |
| `speciality` | `t_users.f_speciality_name` | прямое; роль `doctor` |

## CSV → пациенты (`t_users`, роль `patient`)

Эндпоинт: `POST /api/v1/admins/import/patients`.

| Поле CSV | Поле БД | Преобразование |
|---|---|---|
| `login` | `t_users.f_login` | нормализация; проверка уникальности |
| `fullName` | `t_users.f_fio` | прямое |
| `policyNumber` | `t_users.f_policy_number` | 16 цифр, если передан |

## CSV → рабочие слоты (`t_work_slots`)

Эндпоинт: `POST /api/v1/admins/import/work-slots`.

| Поле CSV | Поле БД | Преобразование |
|---|---|---|
| `doctorLogin` | `t_work_slots.f_doctor_id` | поиск врача по логину |
| `date` | `t_work_slots.f_date` | `yyyy-MM-dd` |
| `startTime` | `f_start_time` | `HH:mm` |
| `endTime` | `f_end_time` | `HH:mm` |
| `slotMinutes` | `f_slot_minutes` | целое; генератор приводит к 60 |
| `breakStart` | `f_break_start` | `HH:mm`, nullable |
| `breakEnd` | `f_break_end` | `HH:mm`, nullable |

## CSV → записи (`t_appointments`)

Эндпоинт: `POST /api/v1/admins/import/appointments`.

| Поле CSV | Поле БД | Преобразование |
|---|---|---|
| `patientLogin` | `t_appointments.f_patient_id` | поиск пациента по логину |
| `doctorLogin` | `t_appointments.f_doctor_id` | поиск врача по логину |
| `slotDateTime` | `f_slot_datetime` | ISO `yyyy-MM-dd'T'HH:mm[:ss]` |
| `status` | `f_status_id` | `booked`, `canceled`, `completed` |

## CSV-экспорт сущностей

Эндпоинты:
- `GET /api/v1/admins/export/doctors`
- `GET /api/v1/admins/export/patients`
- `GET /api/v1/admins/export/appointments`
- `GET /api/v1/admins/export/work-slots`

Ответ — `CsvExportResultDto`: `file_name`, `rows_exported`, `timestamp`, `status`, `message`, `csv_content`.

## Отчёт утилизации → CSV

Эндпоинт: `GET /api/v1/reports/utilization/export?from=...&to=...&doctorId=...&speciality=...`.

| Поле CSV | Источник | Описание |
|---|---|---|
| `doctor_id` | `t_users.f_users_id` (роль `doctor`) | ID врача |
| `date` | `t_work_slots.f_date` | Дата |
| `slots_total` | сгенерированные слоты за дату | Всего доступных слотов |
| `slots_booked` | `t_appointments` со статусом ≠ `canceled` | Занятые слоты |
| `utilization` | `slots_booked / slots_total` | 0.0000–1.0000 |

## API-контуры → данные

| Эндпоинт | Источник | Назначение | Формат |
|---|---|---|---|
| `GET /api/v1/doctors` | `t_users` (роль `doctor`) | список врачей | JSON |
| `GET /api/v1/doctors/{id}/slots` | `t_work_slots`, `t_appointments` | свободные слоты | JSON |
| `POST /api/v1/appointments` | → `t_appointments` | запись регистратором | JSON |
| `POST /api/v1/appointments/me` | → `t_appointments` | запись пациентом | JSON |
| `GET /api/v1/appointments/{id}/history` | `t_appointment_operations` | журнал операций | JSON |
| `POST /api/v1/patients` | → `t_users` | создание пациента | JSON |
| `GET /api/v1/reports/utilization` | `t_work_slots`, `t_appointments` | отчёт | JSON |
| `GET /api/v1/reports/utilization/export` | те же | экспорт | `text/csv` |

## Идемпотентность CSV

Повторная загрузка **не создаёт дублей**: строки с существующим ключом (логин, пара «врач + время», запись на слот) отклоняются и возвращаются в `errors` с номером строки; итоговый статус — `PARTIAL` или `FAILED`. Это не «тихий пропуск»: администратор видит, какие строки не применились.
