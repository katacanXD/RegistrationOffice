# DOC-DAT-004 — DFD (диаграмма потоков данных)

| Версия | Статус | Дата создания | Дата обновления |
|--------|--------|---------------|-----------------|
| v0.3   | Actual | 2026-04-14    | 2026-11-07      |

О документе: диаграмма потоков данных текущего RegistrationOffice без legacy-отчётов и без отдельных таблиц `doctors/patients`.

Для кого: аналитик, backend-разработчик.

Основано на: [ERD](DOC-DAT-001-logical-data-model-and-erd.md), [BPMN](../04-architecture/DOC-ARC-001-business-process-and-user-flow.md).

---

## Авторы документа

- Наботов Карим — project lead

## История изменений

- [v0.1] [2026-04-14] (Наботов): первичная DFD.
- [v0.2] [2026-04-18] (Чернышов): уточнение ролей и потоков данных.

- **Пациент**: вход, привязка полиса, поиск врача, запись, перенос, отмена.
- **Врач**: просмотр календаря и расписания, просмотр талона.
- **Регистратор**: поиск пациента, запись и перенос от имени пациента.
- **Администратор**: управление пользователями и CSV import/export.

## Основные процессы

| № | Процесс | Входные потоки | Выходные потоки | Хранилища |
|---|---------|----------------|-----------------|-----------|
| P1 | Аутентификация | login/password | access token, refresh cookie, user profile | `t_users`, `t_refresh_tokens` |
| P2 | Привязка полиса пациента | policy number | success/error | `t_users` |
| P3 | Поиск врача | search | doctors list | `t_users`, `t_dict_role` |
| P4 | Получение слотов | doctorId + date | slots list, break info | `t_work_slots`, `t_appointments` |
| P5 | Создание записи | doctorId + patientId + slotDatetime | appointment created / error | `t_appointments`, `t_dict_status` |
| P6 | Перенос/отмена записи | appointmentId + new slot or confirm delete | updated appointment / canceled | `t_appointments` |
| P7 | Просмотр расписания врача | doctorId + date/month | calendar, schedule, detail card | `t_work_slots`, `t_appointments` |
| P8 | Админские операции | user search, CSV file | user list, import result | `t_users`, `t_work_slots`, `t_appointments` |
| P9 | Создание пациента регистратором | login, fio, policy, password | patient card / error | `t_users`, `t_dict_role` |
| P10 | Отчёт утилизации | period, doctorId, speciality | rows, totals, CSV | `t_work_slots`, `t_appointments` |
| P11 | Очистка refresh-токенов | расписание (cron) | deleted count | `t_refresh_tokens` |

## Потоки данных между процессами

- P1 → P3/P4/P5/P6/P7/P8: токен и роль определяют доступ к интерфейсу.
- P2 → P3: только пациент с полисом может перейти к поиску врача.
- P3 → P4: выбранный врач передаётся на экран календаря.
- P4 → P5: выбранный слот передаётся в создание записи.
- P5 → P6: созданная запись может быть отменена или перенесена.
- P5/P6 → журнал: операция пишется в `t_appointment_operations` в той же транзакции.
- P7 → P6: из расписания врача можно открыть талон записи.
- P8 → backend CSV import/export: админ загружает или получает данные; отчёт вынесен в отдельный процесс P10.
- P9 → P3: созданный пациент сразу доступен для поиска и записи.
- P10 → P7: отчёт использует сгенерированные слоты и активные записи.
- P11 → P1: очистка удаляет только истёкшие refresh-токены.

## Важные замечания

- Отчёт утилизации есть в UI администратора (вкладка «Загрузка расписания») и в CSV; отдельного хранилища у отчёта нет.
- В текущей модели нет отдельных таблиц `doctors` и `patients` — это роли в `t_users`.
- Журнал операций — `t_appointment_operations`; причина отмены/переноса может быть `null`.
- Для refresh flow используется cookie `refresh_token`, а не тело JSON.
