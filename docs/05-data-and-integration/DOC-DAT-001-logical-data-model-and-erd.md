# DOC-DAT-001 Логическая модель данных и ERD

| Версия | Статус | Дата обновления |
|---|---|---|
| v1.1 | Draft | 2026-04-17 |

О документе: логическая модель данных системы RegistrationOffice — сущности, атрибуты, связи.

Для кого: backend-разработчики, аналитик, тестировщик.

Основано на: [кейсе проекта](../98-source-materials/Кейс_Здравоохранение_График_приёмов_врача.md), [user stories](../02-requirements/DOC-REQ-001-user-stories.md).

---

## Авторы документа

- Чернышов Ярослав — backend developer — проектирование модели
- Наботов Карим — project lead — валидация

## История изменений

- [v0.1] [2026-04-08] (Чернышов): первичная ER-диаграмма.
- [v0.2] [2026-04-10] (Чернышов, Наботов): добавлены таблицы users и roles для JWT.

---

## ER-диаграмма (текстовое представление)

```
┌──────────────┐       ┌──────────────────┐       ┌──────────────┐
│   doctors    │       │   work_slots     │       │   patients   │
├──────────────┤       ├──────────────────┤       ├──────────────┤
│ doctor_id PK │──┐    │ slot_id PK       │       │ patient_code │
│ fio          │  │    │ doctor_id FK ────┼───────│   PK         │
│ specialty    │  │    │ date             │       │ fio          │
│ user_id FK   │  │    │ start_time       │       │ birth_year   │
└──────────────┘  │    │ end_time         │       │ user_id FK   │
                  │    │ slot_minutes     │       └──────┬───────┘
                  │    │ break_start      │              │
                  │    │ break_end        │              │
                  │    └────────┬─────────┘              │
                  │             │                        │
                  │    ┌────────▼─────────┐              │
                  │    │  appointments    │              │
                  │    ├──────────────────┤              │
                  └───▶│ appt_id PK       │              │
                       │ doctor_id FK     │              │
                       │ slot_datetime    │◀─────────────┘
                       │ status           │   patient_code FK
                       │ patient_code FK  │
                       │ created_at       │
                       │ updated_at       │
                       │ cancel_reason    │
                       │ created_by       │
                       └──────────────────┘

┌──────────────┐       ┌──────────────────┐
│    users     │       │     roles        │
├──────────────┤       ├──────────────────┤
│ user_id PK   │       │ role_id PK       │
│ username     │       │ name             │
│ password_hash│       │ (PATIENT, DOCTOR,│
│ role_id FK   │       │  REGISTRAR,      │
│ enabled      │       │  ADMIN)          │
└──────────────┘       └──────────────────┘
```

## Сущности и связи

| Связь                        | Тип   | Описание                                               |
|------------------------------|-------|---------------------------------------------------------|
| doctors → work_slots         | 1 : N | У врача может быть много рабочих слотов                 |
| doctors → appointments       | 1 : N | У врача может быть много записей на приём                |
| patients → appointments      | 1 : N | У пациента может быть много записей                      |
| users → doctors              | 1 : 1 | Врач привязан к учётной записи                           |
| users → patients             | 1 : 1 | Пациент привязан к учётной записи                        |
| roles → users                | 1 : N | У роли может быть много пользователей                    |

## Нормализация

Модель приведена к 3НФ:

- **1НФ**: все атрибуты атомарны, нет повторяющихся групп.
- **2НФ**: все неключевые атрибуты зависят от полного первичного ключа (ключи — одноатрибутные).
- **3НФ**: нет транзитивных зависимостей (specialty зависит только от doctor_id, а не через промежуточный атрибут).

## Ограничения целостности

| Ограничение                              | Реализация                                |
|------------------------------------------|-------------------------------------------|
| Уникальность doctor_id + slot_datetime   | UNIQUE constraint на appointments          |
| status ∈ {booked, canceled, completed}   | CHECK constraint                           |
| doctor_id → doctors.doctor_id            | FOREIGN KEY                                |
| patient_code → patients.patient_code     | FOREIGN KEY                                |
| slot_datetime внутри рабочего интервала  | Проверка на уровне бизнес-логики (backend) |

## Примечания по backend-реализации

- `refresh_token` хранится в отдельной таблице и передается через `HttpOnly` cookie, не через JSON;
- при логине/refresh backend создает новый access token и обновляет refresh token;
- `speciality` хранится в `t_users.f_speciality_name` и используется только для роли `doctor`.

