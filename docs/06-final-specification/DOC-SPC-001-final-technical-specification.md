# DOC-SPC-001 Финальная техническая спецификация

| Версия | Статус | Дата обновления |
|---|---|---|
| v1.1 | Draft | 2026-04-17 |

## Область решения

MVP включает fullstack-контур: frontend SPA + backend API + PostgreSQL + Docker Compose deployment.

## Функциональные модули

- Auth и безопасность (`login`, `registration`, `refresh`, `logout`, `me`)
- Пользователи и роли (`/users`, `/patients`)
- Врачи и расписание (`/doctors`, public slots/calendar)
- Записи на прием (`/appointments`)
- Администрирование CSV (`/admins/export/*`, `/admins/import/*`)

## Нефункциональные требования

- role-based access;
- refresh token в HttpOnly cookie;
- предсказуемые HTTP-коды и ошибки;
- воспроизводимый деплой на VM;
- обновляемая документация по контрактам.
