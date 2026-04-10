# Документация RegistrationOffice

Эта папка содержит проектную и эксплуатационную документацию для fullstack-проекта `RegistrationOffice` (backend + frontend).

## Быстрая навигация

- Основной обзор проекта: `README.md`
- Технический runbook: `HELP.md`
- Деплой на VM через Docker Compose: `DEPLOY_YC_DOCKER.md`

## Что сейчас есть в `docs/`

- `docs/README.md` — этот файл (карта документации)
- `docs/docs/` — основной комплект проектной документации (GOV/DIS/REQ/PLN/ARC/DAT/INT/SPC/TST/OPS)

## Текущие источники истины в коде

При обновлении документации ориентируйтесь на:

- API-контроллеры: `backend/src/main/java/ru/katacan/registrationoffice/controller`
- Security и доступы: `backend/src/main/java/ru/katacan/registrationoffice/config/SecurityConfig.java`
- Модель данных (JPA сущности): `backend/src/main/java/ru/katacan/registrationoffice/entity`
- Конфигурация приложения: `backend/src/main/resources/application.properties`
- Контейнеризация и прокси: `backend/Dockerfile`, `frontend/Dockerfile`, `frontend/nginx/default.conf`, `docker-compose.yml`, `deploy/nginx/default.conf`

## ERD (Mermaid)

```mermaid
erDiagram
    t_users {
        BIGINT f_users_id PK
        VARCHAR f_login "NOT NULL, UNIQUE"
        VARCHAR f_fio "NOT NULL"
        VARCHAR f_password "NOT NULL"
        VARCHAR f_policy_number "NULL"
        BIGINT f_role_id FK
        VARCHAR f_speciality_name "NULL"
    }

    t_dict_role {
        BIGINT f_role_id PK
        VARCHAR f_role_name "NOT NULL, UNIQUE"
    }

    t_dict_status {
        BIGINT f_status_id PK
        VARCHAR f_status_name "NOT NULL, UNIQUE"
    }

    t_appointments {
        BIGINT f_appoint_id PK
        BIGINT f_doctor_id FK
        DATETIME f_slot_datetime
        BIGINT f_status_id FK
        BIGINT f_patient_id FK
    }

    t_work_slots {
        BIGINT f_work_slots_id PK
        BIGINT f_doctor_id FK
        DATE f_date
        TIME f_start_time
        TIME f_end_time
        INT f_slot_minutes
        TIME f_break_start
        TIME f_break_end
    }

    t_refresh_tokens {
        BIGINT f_token_id PK
        VARCHAR f_token_hash "NOT NULL, UNIQUE"
        BIGINT f_user_id FK
        TIMESTAMP f_expiry_date
        TIMESTAMP f_created_at
        TIMESTAMP f_revoked_at
        VARCHAR f_replaced_by_token_hash
    }

    t_dict_role ||--o{ t_users : "f_role_id"
    t_users ||--o{ t_refresh_tokens : "f_user_id"
    t_users ||--o{ t_work_slots : "doctor"
    t_dict_status ||--o{ t_appointments : "f_status_id"
    t_users ||--o{ t_appointments : "doctor"
    t_users ||--o{ t_appointments : "patient"
```

## Правило обновления документации

Любое изменение API, ролей доступа, env-переменных или структуры БД должно сопровождаться обновлением:

1. `README.md`
2. `HELP.md`
3. `DEPLOY_YC_DOCKER.md` (если затронут деплой)
4. `docs/README.md` (если изменена карта документации)
