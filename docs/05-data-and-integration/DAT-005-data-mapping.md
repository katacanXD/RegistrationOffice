# DOC-DAT-005 — Таблица маппинга данных

| Версия | Статус | Дата создания | Дата обновления |
|--------|--------|---------------|-----------------|
| v0.1   | Draft  | 2026-04-14    | 2026-04-14      |

О документе: описание маппинга данных между модулями системы и внешними источниками (CSV-импорт).

Для кого: backend-разработчик, аналитик.

Основано на: [ERD](DOC-DAT-001-erd.md), [API](DOC-DAT-003-api-specification.md), [DFD](DOC-DAT-004-dfd.md).

---

## Авторы документа

- Наботов Карим — project lead

## История изменений

- [v0.1] [2026-04-14] (Наботов): первичный маппинг.
- [v0.2] [2026-04-18] (Чернышов): добавлены маппинги для отчёта утилизации и API.

---

## Маппинг: CSV → work_slots

Источник: CSV-файл, загружаемый через `/api/work-slots/import-csv`.

| Поле CSV      | Поле БД (work_slots) | Тип        | Преобразование                |
|---------------|----------------------|------------|-------------------------------|
| doctor_id     | doctor_id            | BIGINT     | Прямое; FK на doctors          |
| date          | date                 | DATE       | Парсинг YYYY-MM-DD            |
| start_time    | start_time           | TIME       | Парсинг HH:MM                 |
| end_time      | end_time             | TIME       | Парсинг HH:MM                 |
| slot_minutes  | slot_minutes         | INTEGER    | Прямое                        |
| break_start   | break_start          | TIME       | Парсинг HH:MM (nullable)      |
| break_end     | break_end            | TIME       | Парсинг HH:MM (nullable)      |

Идемпотентность: при повторной загрузке записи с одинаковыми (doctor_id, date, start_time) пропускаются.

## Маппинг: CSV → appointments

Источник: CSV-файл для массовой загрузки записей.

| Поле CSV       | Поле БД (appointments) | Тип        | Преобразование               |
|----------------|------------------------|------------|------------------------------|
| doctor_id      | doctor_id              | BIGINT     | FK на doctors                 |
| slot_datetime  | slot_datetime          | TIMESTAMP  | Парсинг ISO 8601              |
| status         | status                 | VARCHAR    | Валидация ∈ {booked, canceled, completed} |
| patient_code   | patient_code           | BIGINT     | FK на patients                |

## Маппинг: appointments → CSV-экспорт (отчёт утилизации)

Назначение: эндпоинт `/api/reports/utilization/export-csv`.

| Поле CSV       | Источник данных                | Описание                      |
|----------------|--------------------------------|-------------------------------|
| doctor_id      | doctors.doctor_id              | ID врача                      |
| date           | work_slots.date                | Дата                          |
| slots_total    | COUNT(generated slots)         | Общее кол-во доступных слотов |
| slots_booked   | COUNT(appointments.status='booked') | Кол-во занятых слотов    |
| utilization    | slots_booked / slots_total     | Утилизация (0.00 – 1.00)     |

## Маппинг: API запрос → ответ (ключевые эндпоинты)

| Эндпоинт                 | Источник (БД)       | Назначение (клиент)     | Формат         |
|---------------------------|---------------------|-------------------------|----------------|
| GET /api/doctors          | doctors             | Frontend: список врачей | JSON array     |
| GET /api/work-slots       | work_slots          | Frontend: сетка слотов  | JSON array     |
| POST /api/appointments    | → appointments      | Создание записи         | JSON object    |
| GET /api/reports/utilization | work_slots + appointments | Отчёт утилизации | JSON array  |
| GET /api/reports/.../csv  | work_slots + appointments | Экспорт             | text/csv       |
