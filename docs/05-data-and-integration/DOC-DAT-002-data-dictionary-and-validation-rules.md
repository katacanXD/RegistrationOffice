# DOC-DAT-002 — Словарь данных

| Версия | Статус | Дата создания | Дата обновления |
|--------|--------|---------------|-----------------|
| v0.1   | Draft  | 2026-04-10    | 2026-04-10      |

О документе: подробное описание всех таблиц, полей, типов данных и ограничений.

Для кого: backend-разработчики, тестировщик.

Основано на: [ERD](DOC-DAT-001-erd.md).
| Версия | Статус | Дата обновления |
|---|---|---|
| v1.1 | Draft | 2026-04-17 |

---

## Авторы документа
| Поле | Тип | Ограничения | Назначение |
|---|---|---|---|
| `f_users_id` | BIGINT | PK, AUTO_INCREMENT | Уникальный идентификатор пользователя |
| `f_login` | VARCHAR(64) | NOT NULL, UNIQUE | Логин для входа; допустимы латинские буквы, цифры, `.`, `_`, `-` |
| `f_fio` | VARCHAR(255) | NOT NULL | Полное имя и отчество |
| `f_password` | VARCHAR(255) | NOT NULL | Хэш пароля (BCrypt) |
| `f_policy_number` | VARCHAR(30) | NULL | Номер полиса (для пациента); в API ожидается 16 цифр |
| `f_role_id` | BIGINT | NOT NULL, FK | Ссылка на роль |
| `f_speciality_name` | VARCHAR(255) | NULL | Специальность (для врача) |

- Чернышов Ярослав — backend developer
- Наботов Карим — project lead

## История изменений
| Поле | Тип | Ограничения | Назначение |
|---|---|---|---|
| `f_role_id` | BIGINT | PK, AUTO_INCREMENT | Идентификатор роли |
| `f_role_name` | VARCHAR(50) | NOT NULL, UNIQUE | Название роли: `patient`, `doctor`, `registrar`, `admin` |

- [v0.1] [2026-04-10] (Чернышов, Наботов): первичный словарь.

---
| Поле | Тип | Ограничения | Назначение |
|---|---|---|---|
| `f_status_id` | BIGINT | PK, AUTO_INCREMENT | Идентификатор статуса |
| `f_status_name` | VARCHAR(50) | NOT NULL, UNIQUE | Название статуса: `booked`, `canceled`, `completed` |

## Таблица: doctors

Назначение: справочник врачей.

| Поле       | Тип данных     | PK/FK | NOT NULL | Описание                     |
|------------|----------------|-------|----------|------------------------------|
| doctor_id  | BIGINT (AUTO)  | PK    | ✓        | Уникальный идентификатор      |
| fio        | VARCHAR(255)   |       | ✓        | ФИО врача                     |
| specialty  | VARCHAR(100)   |       | ✓        | Специальность                 |
| user_id    | BIGINT         | FK    |          | Ссылка на учётную запись      |

## Таблица: patients

Назначение: справочник пациентов.

| Поле          | Тип данных     | PK/FK | NOT NULL | Описание                     |
|---------------|----------------|-------|----------|------------------------------|
| patient_code  | BIGINT (AUTO)  | PK    | ✓        | Уникальный код пациента       |
| fio           | VARCHAR(255)   |       | ✓        | ФИО пациента                  |
| birth_year    | INTEGER        |       |          | Год рождения                  |
| user_id       | BIGINT         | FK    |          | Ссылка на учётную запись      |

## Таблица: work_slots
## Правила валидации API

### Auth

1. `login` обязателен, длина 3–64 символа, только `[a-zA-Z0-9_.-]`.
2. `fio` обязателен, длина 2–120 символов.
3. `password` обязателен, длина 8–72 символа.
4. `registration` не принимает роль — новый пользователь создается как `patient`.
5. `refresh` и `logout` используют cookie `refresh_token`.

### Users

1. `ChangePasswordRequestDto`: `old_password` и `new_password` обязательны, длина 8–72 символа.
2. `PolicyRequestDto`: `policy_number` обязателен и должен содержать ровно 16 цифр.
3. `UpdateUserRequestDto`: хотя бы одно поле должно быть заполнено; `role` — только `admin`, `doctor`, `registrar`, `patient`.
4. `speciality` разрешено только для роли `doctor`, `policy_number` — только для роли `patient`.

### Appointments

1. `CreateAppointmentRequestDto`: `doctor_id`, `patient_id`, `slot_datetime` обязательны.
2. `CreateOwnAppointmentRequestDto`: `doctor_id` и `slot_datetime` обязательны; `patient_id` подставляется backend'ом.
3. `slot_datetime` должен быть в формате `yyyy-MM-dd'T'HH:mm[:ss]`.
4. Нельзя создавать или переносить запись на прошедшее время.
5. Запись возможна только внутри рабочего слота врача и вне перерыва.
6. Нельзя создать вторую активную запись на тот же слот.

### Doctors / work slots

1. `GET /api/v1/doctors` принимает только `search`, `page`, `size`; фильтра по `specialty` в API нет.
2. `GET /slots` и `GET /public/slots` принимают `date=yyyy-MM-dd`.
3. `GET /calendar` и `GET /public/calendar` принимают `month` и `year` как целые числа.

### CSV import/export

1. Экспорт возвращает CSV-контент и метаданные операции.
2. Импорт врачей: `login`, `fullName`, `speciality`.
3. Импорт пациентов: `login`, `fullName`, `policyNumber`.
4. Импорт записей: `patientLogin`, `doctorLogin`, `slotDateTime`, `status`.
5. Импорт рабочих слотов: `doctorLogin`, `date`, `startTime`, `endTime`, `slotMinutes`, `breakStart`, `breakEnd`.

## Обязательные валидации

Назначение: рабочие интервалы врачей (из них генерируются слоты для записи).
1. **Двойное бронирование**: для каждого слота не более одной активной записи (status = 'booked')
2. **Слот в рабочем интервале**: `f_start_time` <= слот < `f_end_time`
3. **Слот не в перерыве**: если `f_break_start` и `f_break_end` заданы, слот не должен попадать в интервал
4. **Корректный статус**: только значения из `t_dict_status`
5. **Ссылаемые сущности существуют**: врач и пациент в таблице `t_users`
6. **Статус completed**: устанавливается только после `f_slot_datetime`
7. **Уникальность логина**: `f_login` уникален в таблице `t_users`
8. **Уникальность токена**: `f_token_hash` уникален в таблице `t_refresh_tokens`
9. **Временная зона и формат**: все временные значения передаются в ISO-формате, без локализованных строк.

| Поле          | Тип данных     | PK/FK | NOT NULL | Описание                              |
|---------------|----------------|-------|----------|---------------------------------------|
| slot_id       | BIGINT (AUTO)  | PK    | ✓        | Уникальный идентификатор               |
| doctor_id     | BIGINT         | FK    | ✓        | Ссылка на врача                        |
| date          | DATE           |       | ✓        | Дата рабочего дня                      |
| start_time    | TIME           |       | ✓        | Начало рабочего интервала              |
| end_time      | TIME           |       | ✓        | Конец рабочего интервала               |
| slot_minutes  | INTEGER        |       | ✓        | Длительность одного слота (минуты)     |
| break_start   | TIME           |       |          | Начало перерыва (может быть NULL)      |
| break_end     | TIME           |       |          | Конец перерыва (может быть NULL)       |

Ограничения: start_time < end_time; break_start < break_end; перерыв внутри рабочего интервала.

## Таблица: appointments

Назначение: записи на приём.

| Поле           | Тип данных     | PK/FK | NOT NULL | Описание                              |
|----------------|----------------|-------|----------|---------------------------------------|
| appt_id        | BIGINT (AUTO)  | PK    | ✓        | Уникальный идентификатор записи        |
| doctor_id      | BIGINT         | FK    | ✓        | Ссылка на врача                        |
| slot_datetime  | TIMESTAMP      |       | ✓        | Дата и время слота                     |
| status         | VARCHAR(20)    |       | ✓        | Статус: booked / canceled / completed  |
| patient_code   | BIGINT         | FK    | ✓        | Ссылка на пациента                     |
| created_at     | TIMESTAMP      |       | ✓        | Дата создания записи                   |
| updated_at     | TIMESTAMP      |       |          | Дата последнего изменения              |
| cancel_reason  | VARCHAR(500)   |       |          | Причина отмены (при status=canceled)   |
| created_by     | VARCHAR(100)   |       |          | Кто создал запись (логин)              |

Ограничения: UNIQUE (doctor_id, slot_datetime) для записей со status='booked'; CHECK (status IN ('booked','canceled','completed')).

## Таблица: users

Назначение: учётные записи пользователей для аутентификации.

| Поле           | Тип данных     | PK/FK | NOT NULL | Описание                        |
|----------------|----------------|-------|----------|---------------------------------|
| user_id        | BIGINT (AUTO)  | PK    | ✓        | Уникальный идентификатор         |
| username       | VARCHAR(100)   |       | ✓        | Логин (уникальный)               |
| password_hash  | VARCHAR(255)   |       | ✓        | Хеш пароля (BCrypt)              |
| role_id        | BIGINT         | FK    | ✓        | Ссылка на роль                   |
| enabled        | BOOLEAN        |       | ✓        | Активна ли учётная запись        |

## Таблица: roles

Назначение: справочник ролей.

| Поле    | Тип данных     | PK/FK | NOT NULL | Описание                        |
|---------|----------------|-------|----------|---------------------------------|
| role_id | BIGINT (AUTO)  | PK    | ✓        | Уникальный идентификатор         |
| name    | VARCHAR(50)    |       | ✓        | Название роли (PATIENT, DOCTOR, REGISTRAR, ADMIN) |
- frontend валидирует форму перед отправкой на backend;
- backend выполняет окончательную валидацию перед сохранением;
- отмененная запись не считается доступным слотом;
- завершенная запись (`completed`) фиксируется только после факта приема.
- refresh token не попадает в JSON-ответы и должен храниться в cookie; access token передается в теле ответа.
