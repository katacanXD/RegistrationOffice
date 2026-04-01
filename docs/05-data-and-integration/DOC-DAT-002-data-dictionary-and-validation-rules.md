# DOC-DAT-002 Словарь данных и правила валидации

| Версия | Статус | Дата обновления |
|---|---|---|
| v1.0 | Draft | 2026-04-01 |

## Таблица: t_users

| Поле | Тип | Ограничения | Назначение |
|---|---|---|---|
| `f_users_id` | BIGINT | PK, AUTO_INCREMENT | Уникальный идентификатор пользователя |
| `f_login` | VARCHAR(255) | NOT NULL, UNIQUE | Логин для входа |
| `f_fio` | VARCHAR(255) | NOT NULL | Полное имя и отчество |
| `f_password` | VARCHAR(255) | NOT NULL | Хэш пароля (BCrypt) |
| `f_policy_number` | VARCHAR(30) | NULL | Номер полиса (для пациента) |
| `f_role_id` | BIGINT | NOT NULL, FK | Ссылка на роль |
| `f_speciality_name` | VARCHAR(255) | NULL | Специальность (для врача) |

## Таблица: t_dict_role

| Поле | Тип | Ограничения | Назначение |
|---|---|---|---|
| `f_role_id` | BIGINT | PK, AUTO_INCREMENT | Идентификатор роли |
| `f_role_name` | VARCHAR(50) | NOT NULL, UNIQUE | Название роли: patient, doctor, registrar, admin |

## Таблица: t_dict_status

| Поле | Тип | Ограничения | Назначение |
|---|---|---|---|
| `f_status_id` | BIGINT | PK, AUTO_INCREMENT | Идентификатор статуса |
| `f_status_name` | VARCHAR(50) | NOT NULL, UNIQUE | Название статуса: booked, canceled, completed |

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

## Обязательные валидации

1. **Двойное бронирование**: для каждого слота не более одной активной записи (status = 'booked')
2. **Слот в рабочем интервале**: `f_start_time` <= слот < `f_end_time`
3. **Слот не в перерыве**: если `f_break_start` и `f_break_end` заданы, слот не должен попадать в интервал
4. **Корректный статус**: только значения из `t_dict_status`
5. **Ссылаемые сущности существуют**: врач и пациент в таблице `t_users`
6. **Статус completed**: устанавливается только после `f_slot_datetime`
7. **Уникальность логина**: `f_login` уникален в таблице `t_users`
8. **Уникальность токена**: `f_token_hash` уникален в таблице `t_refresh_tokens`

## Правила качества данных

- frontend валидирует форму перед отправкой на backend;
- backend выполняет окончательную валидацию перед сохранением;
- отмененная запись не считается доступным слотом;
- завершенная запись (`completed`) фиксируется только после факта приема.
