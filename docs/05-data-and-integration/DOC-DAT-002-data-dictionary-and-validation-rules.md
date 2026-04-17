# DOC-DAT-002 Словарь данных и правила валидации

| Версия | Статус | Дата обновления |
|---|---|---|
| v1.1 | Draft | 2026-04-17 |

## Таблица: t_users

| Поле | Тип | Ограничения | Назначение |
|---|---|---|---|
| `f_users_id` | BIGINT | PK, AUTO_INCREMENT | Уникальный идентификатор пользователя |
| `f_login` | VARCHAR(64) | NOT NULL, UNIQUE | Логин для входа; допустимы латинские буквы, цифры, `.`, `_`, `-` |
| `f_fio` | VARCHAR(255) | NOT NULL | Полное имя и отчество |
| `f_password` | VARCHAR(255) | NOT NULL | Хэш пароля (BCrypt) |
| `f_policy_number` | VARCHAR(30) | NULL | Номер полиса (для пациента); в API ожидается 16 цифр |
| `f_role_id` | BIGINT | NOT NULL, FK | Ссылка на роль |
| `f_speciality_name` | VARCHAR(255) | NULL | Специальность (для врача) |

## Таблица: t_dict_role

| Поле | Тип | Ограничения | Назначение |
|---|---|---|---|
| `f_role_id` | BIGINT | PK, AUTO_INCREMENT | Идентификатор роли |
| `f_role_name` | VARCHAR(50) | NOT NULL, UNIQUE | Название роли: `patient`, `doctor`, `registrar`, `admin` |

## Таблица: t_dict_status

| Поле | Тип | Ограничения | Назначение |
|---|---|---|---|
| `f_status_id` | BIGINT | PK, AUTO_INCREMENT | Идентификатор статуса |
| `f_status_name` | VARCHAR(50) | NOT NULL, UNIQUE | Название статуса: `booked`, `canceled`, `completed` |

## Таблица: t_work_slots

| Поле | Тип | Ограничения | Назначение |
|---|---|---|---|
| `f_work_slots_id` | BIGINT | PK, AUTO_INCREMENT | Уникальный идентификатор слота |
| `f_doctor_id` | BIGINT | NOT NULL, FK | Ссылка на врача |
| `f_date` | DATE | NOT NULL | Дата рабочего интервала |
| `f_start_time` | TIME | NOT NULL | Начало рабочего интервала |
| `f_end_time` | TIME | NOT NULL | Конец рабочего интервала |
| `f_slot_minutes` | INT | NOT NULL | Длительность одного слота (в минутах) |
| `f_break_start` | TIME | NULL | Начало перерыва (если есть) |
| `f_break_end` | TIME | NULL | Конец перерыва (если есть) |

## Таблица: t_appointments

| Поле | Тип | Ограничения | Назначение |
|---|---|---|---|
| `f_appoint_id` | BIGINT | PK, AUTO_INCREMENT | Уникальный идентификатор записи |
| `f_doctor_id` | BIGINT | NOT NULL, FK | Ссылка на врача |
| `f_patient_id` | BIGINT | NOT NULL, FK | Ссылка на пациента |
| `f_slot_datetime` | DATETIME | NOT NULL | Дата и время приема |
| `f_status_id` | BIGINT | NOT NULL, FK | Ссылка на статус |

## Таблица: t_refresh_tokens

| Поле | Тип | Ограничения | Назначение |
|---|---|---|---|
| `f_token_id` | BIGINT | PK, AUTO_INCREMENT | Идентификатор токена |
| `f_user_id` | BIGINT | NOT NULL, FK | Ссылка на пользователя |
| `f_token_hash` | VARCHAR(512) | NOT NULL, UNIQUE | Хэш refresh-токена |
| `f_expiry_date` | TIMESTAMP | NOT NULL | Время истечения токена |
| `f_created_at` | TIMESTAMP | NOT NULL | Дата/время создания |
| `f_revoked_at` | TIMESTAMP | NULL | Дата/время отзыва (если отозван) |
| `f_replaced_by_token_hash` | VARCHAR(512) | NULL | Хэш заменяющего токена (при refresh) |

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

1. **Двойное бронирование**: для каждого слота не более одной активной записи (status = 'booked')
2. **Слот в рабочем интервале**: `f_start_time` <= слот < `f_end_time`
3. **Слот не в перерыве**: если `f_break_start` и `f_break_end` заданы, слот не должен попадать в интервал
4. **Корректный статус**: только значения из `t_dict_status`
5. **Ссылаемые сущности существуют**: врач и пациент в таблице `t_users`
6. **Статус completed**: устанавливается только после `f_slot_datetime`
7. **Уникальность логина**: `f_login` уникален в таблице `t_users`
8. **Уникальность токена**: `f_token_hash` уникален в таблице `t_refresh_tokens`
9. **Временная зона и формат**: все временные значения передаются в ISO-формате, без локализованных строк.

## Правила качества данных

- frontend валидирует форму перед отправкой на backend;
- backend выполняет окончательную валидацию перед сохранением;
- отмененная запись не считается доступным слотом;
- завершенная запись (`completed`) фиксируется только после факта приема.
- refresh token не попадает в JSON-ответы и должен храниться в cookie; access token передается в теле ответа.
