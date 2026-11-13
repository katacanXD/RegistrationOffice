# DOC-DAT-002 Словарь данных и правила валидации

| Версия | Статус | Дата обновления |
|---|---|---|
| v1.4 | Actual | 2026-11-07 |

О документе: словарь данных backend RegistrationOffice и фактические правила валидации DTO.

---

## Основные сущности

### `t_users`

| Поле | Тип | Ограничения | Назначение |
|---|---|---|---|
| `f_users_id` | BIGINT | PK, AUTO_INCREMENT | Идентификатор пользователя |
| `f_login` | VARCHAR(64) | NOT NULL, UNIQUE | Логин |
| `f_fio` | VARCHAR(120) | NOT NULL | ФИО |
| `f_password` | VARCHAR(255) | NOT NULL | Хэш пароля |
| `f_policy_number` | VARCHAR(16) | NULL | Номер полиса пациента |
| `f_role_id` | BIGINT | NOT NULL, FK | Роль |
| `f_speciality_name` | VARCHAR(120) | NULL | Специальность врача |

### `t_dict_role`

| Поле | Тип | Ограничения | Назначение |
|---|---|---|---|
| `f_role_id` | BIGINT | PK, AUTO_INCREMENT | Идентификатор роли |
| `f_role_name` | VARCHAR(30) | NOT NULL, UNIQUE | `patient`, `doctor`, `registrar`, `admin` |

### `t_dict_status`

| Поле | Тип | Ограничения | Назначение |
|---|---|---|---|
| `f_status_id` | BIGINT | PK, AUTO_INCREMENT | Идентификатор статуса |
| `f_status_name` | VARCHAR(50) | NOT NULL, UNIQUE | `booked`, `canceled`, `completed` |

### `t_work_slots`

| Поле | Тип | Ограничения | Назначение |
|---|---|---|---|
| `f_work_slots_id` | BIGINT | PK, AUTO_INCREMENT | Идентификатор рабочего интервала |
| `f_doctor_id` | BIGINT | NOT NULL, FK | Врач |
| `f_date` | DATE | NOT NULL | Дата |
| `f_start_time` | TIME | NOT NULL | Начало интервала |
| `f_end_time` | TIME | NOT NULL | Конец интервала |
| `f_slot_minutes` | INT | NOT NULL | Длительность слота |
| `f_break_start` | TIME | NULL | Начало перерыва |
| `f_break_end` | TIME | NULL | Конец перерыва |

### `t_appointments`

| Поле | Тип | Ограничения | Назначение |
|---|---|---|---|
| `f_appoint_id` | BIGINT | PK, AUTO_INCREMENT | Идентификатор записи |
| `f_doctor_id` | BIGINT | NOT NULL, FK | Врач |
| `f_patient_id` | BIGINT | NOT NULL, FK | Пациент |
| `f_slot_datetime` | DATETIME | NOT NULL | Дата и время записи |
| `f_status_id` | BIGINT | NOT NULL, FK | Статус |

### `t_appointment_operations`

| Поле | Тип | Ограничения | Назначение |
|---|---|---|---|
| `f_operation_id` | BIGINT | PK, AUTO_INCREMENT | Идентификатор операции |
| `f_appointment_id` | BIGINT | NOT NULL, FK | Запись |
| `f_operation_type` | VARCHAR(32) | NOT NULL | `created`, `rescheduled`, `canceled` |
| `f_actor_id` | BIGINT | NULL, FK | Кто выполнил; `null` для системных операций |
| `f_occurred_at` | TIMESTAMP | NOT NULL | Когда выполнено |
| `f_reason` | VARCHAR(255) | NULL | Причина отмены/переноса |
| `f_old_slot_datetime` | TIMESTAMP | NULL | Старое время |
| `f_new_slot_datetime` | TIMESTAMP | NULL | Новое время |
| `f_old_status` | VARCHAR(50) | NULL | Старый статус |
| `f_new_status` | VARCHAR(50) | NULL | Новый статус |

### `t_refresh_tokens`

| Поле | Тип | Ограничения | Назначение |
|---|---|---|---|
| `f_token_id` | BIGINT | PK, AUTO_INCREMENT | Идентификатор токена |
| `f_token_hash` | VARCHAR(512) | NOT NULL, UNIQUE | Хэш refresh token |
| `f_user_id` | BIGINT | NOT NULL, FK | Пользователь |
| `f_expiry_date` | TIMESTAMP | NOT NULL | Срок действия |
| `f_created_at` | TIMESTAMP | NOT NULL | Дата создания |
| `f_revoked_at` | TIMESTAMP | NULL | Дата отзыва |
| `f_replaced_by_token_hash` | VARCHAR(512) | NULL | Токен при ротации |

---

## Правила валидации API

### Auth

1. `login` обязателен, 3–64 символа, только `[a-zA-Z0-9_.-]`.
2. `fio` обязателен, 2–120 символов, только буквы, пробелы, `'` и `-`.
3. `password` обязателен, 8–72 символа.
4. Регистрация не принимает роль — создаётся `patient`.
5. `refresh` и `logout` используют cookie `refresh_token`.

### Users

1. `ChangePasswordRequestDto`: `oldPassword` и `newPassword` обязательны, 8–72 символа.
2. `PolicyRequestDto`: `policyNumber` — ровно 16 цифр.
3. `UpdateUserRequestDto.policyNumber` — 16 цифр, если передан.
4. `UpdateUserRequestDto.speciality` — до 120 символов, только буквы/цифры/пробелы/`.,()-`.
5. `UpdateUserRequestDto.role` — `patient`, `doctor`, `registrar`, `admin` в нижнем регистре.

### Appointments

1. `CreateAppointmentRequestDto`: `doctorId`, `patientId`, `slotDatetime` обязательны.
2. `CreateOwnAppointmentRequestDto`: `doctorId`, `slotDatetime` обязательны; `patientId` берётся из JWT.
3. `UpdateAppointmentRequestDto`: `doctorId`, `slotDatetime`, `status`, `reason` опциональны; `reason` ≤ 255 символов.
4. `CancelAppointmentRequestDto`: `reason` опционален, ≤ 255 символов.
5. `slotDatetime` — `yyyy-MM-dd'T'HH:mm[:ss]`.
6. Нельзя создавать/переносить запись на прошедшее время.
7. Нельзя создавать вторую активную запись на тот же слот врача.
8. Нельзя создать/перенести запись, если у пациента уже есть активная запись на это время.
9. Повторный запрос того же пациента на тот же слот возвращает существующую запись (`replayed=true`, HTTP 200).

### Patients

1. `CreatePatientRequestDto`: `login`, `fio`, `password` обязательны; `policyNumber` опционален.
2. `login` — 3–64 символа, `[a-zA-Z0-9_.-]`.
3. `fio` — 2–120 символов, буквы/пробелы/`'`/`-`.
4. `policyNumber` — ровно 16 цифр, если передан.
5. `password` — 8–72 символа.
6. Создание доступно `admin` и `registrar`; роль создаваемого — `patient`.

### Doctors / schedule

1. `GET /api/v1/doctors` принимает `search` и пагинацию (`page`, `size`).
2. `GET /slots` принимает `date=yyyy-MM-dd`.
3. `GET /calendar` принимает `month` и `year`.

### CSV import/export

1. Doctors: `login`, `fullName`, `speciality`.
2. Patients: `login`, `fullName`, `policyNumber`.
3. Appointments: `patientLogin`, `doctorLogin`, `slotDateTime`, `status`.
4. Work slots: `doctorLogin`, `date`, `startTime`, `endTime`, `slotMinutes`, `breakStart`, `breakEnd`.
5. Повторная загрузка не создаёт дублей: конфликтные строки возвращаются в `errors` с номером строки.

### Reports

1. `GET /api/v1/reports/utilization` принимает `from`, `to` (`yyyy-MM-dd`), `doctorId`, `speciality`.
2. Если `from`/`to` не переданы — период «сегодня + 6 дней»; передавать нужно оба.
3. Период — не больше 31 дня.
4. `GET /api/v1/reports/utilization/export` отдаёт CSV с колонками `doctor_id, date, slots_total, slots_booked, utilization`.

## Ключевые инварианты

1. Логин пользователя уникален.
2. Refresh token хранится как хэш и тоже уникален.
3. Отменённая запись не считается активной для выбора слота.
4. У пациента не больше одной активной записи на одно время.
5. Каждая операция создания/переноса/отмены пишется в `t_appointment_operations` в той же транзакции.
6. Полис пациента не уникален в БД — это открытый вопрос; валидация проверяет только формат.
7. Наличие хотя бы одного рабочего слота у врача БД не гарантирует — это открытый вопрос.
8. Пациентский полис должен быть привязан до перехода к поиску врача в UI.
