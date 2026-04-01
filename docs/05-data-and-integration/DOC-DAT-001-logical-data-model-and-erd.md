# DOC-DAT-001 Логическая модель данных и ERD

| Версия | Статус | Дата обновления |
|---|---|---|
| v1.0 | Draft | 2026-04-01 |

## Сущности

- `t_users`
- `t_dict_role`
- `t_dict_status`
- `t_work_slots`
- `t_appointments`
- `t_refresh_tokens`

## Ключевые связи

- `t_users.f_role_id -> t_dict_role.f_role_id`
- `t_work_slots.f_doctor_id -> t_users.f_users_id`
- `t_appointments.f_doctor_id -> t_users.f_users_id`
- `t_appointments.f_patient_id -> t_users.f_users_id`
- `t_appointments.f_status_id -> t_dict_status.f_status_id`
- `t_refresh_tokens.f_user_id -> t_users.f_users_id`

## ERD (Mermaid)

```mermaid
erDiagram
    t_users {
        BIGINT f_users_id PK
        VARCHAR f_login "NOT NULL, UNIQUE"
        VARCHAR f_fio "NOT NULL"
        VARCHAR f_password "NOT NULL"
        VARCHAR f_policy_number "NULL, len=30"
        BIGINT f_role_id FK "NOT NULL"
        VARCHAR f_speciality_name "NULL"
    }

    t_dict_role {
        BIGINT f_role_id PK
        VARCHAR f_role_name "NOT NULL, UNIQUE, len=50"
    }

    t_dict_status {
        BIGINT f_status_id PK
        VARCHAR f_status_name "NOT NULL, UNIQUE, len=50"
    }

    t_appointments {
        BIGINT f_appoint_id PK
        BIGINT f_doctor_id FK "NOT NULL"
        DATETIME f_slot_datetime "NOT NULL"
        BIGINT f_status_id FK "NOT NULL"
        BIGINT f_patient_id FK "NOT NULL"
    }

    t_work_slots {
        BIGINT f_work_slots_id PK
        BIGINT f_doctor_id FK "NOT NULL"
        DATE f_date "NOT NULL"
        TIME f_start_time "NOT NULL"
        TIME f_end_time "NOT NULL"
        INT f_slot_minutes "NOT NULL"
        TIME f_break_start "NULL"
        TIME f_break_end "NULL"
    }

    t_refresh_tokens {
        BIGINT f_token_id PK
        VARCHAR f_token_hash "NOT NULL, UNIQUE, len=512"
        BIGINT f_user_id FK "NOT NULL"
        TIMESTAMP f_expiry_date "NOT NULL"
        TIMESTAMP f_created_at "NOT NULL"
        TIMESTAMP f_revoked_at "NULL"
        VARCHAR f_replaced_by_token_hash "NULL, len=512"
    }

    t_dict_role ||--o{ t_users : "f_role_id"
    t_users ||--o{ t_refresh_tokens : "f_user_id"
    t_users ||--o{ t_work_slots : "doctor (f_doctor_id)"
    t_dict_status ||--o{ t_appointments : "f_status_id"
    t_users ||--o{ t_appointments : "doctor (f_doctor_id)"
    t_users ||--o{ t_appointments : "patient (f_patient_id)"
```

## Примечание для frontend

Frontend должен считать `role` и `status` словарными сущностями, а не хардкодить значения в UI.
