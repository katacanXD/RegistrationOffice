# DOC-SPC-003 Сводка точек интеграции

| Версия | Статус | Дата обновления |
|---|---|---|
| v1.2 | Draft | 2026-04-18 |

## Точки интеграции

1. Frontend <-> Backend API (`/api/v1/**`)
2. Backend <-> PostgreSQL
3. Nginx <-> Backend
4. Nginx <-> Frontend

## Ключевые API-контуры

- Auth: login, refresh, logout, me;
- Users/Patients: профиль, пароль, полис, список пациентов, админский CRUD;
- Doctors: поиск, slots, calendar, schedule, public views;
- Appointments: create/update/cancel/detail;
- Admins: CSV import/export.

## Матрица интеграционных точек

| Точка | Протокол | Владелец | Основной риск |
|---|---|---|---|
| Browser <-> Nginx | HTTP/HTTPS | DevOps | SPA routes или `/api` проксирование |
| Frontend <-> Backend API | HTTP/JSON | Frontend + Backend | DTO, роли, CORS, cookie refresh |
| Backend <-> PostgreSQL | TCP/JDBC | Backend | схема и миграции |
| Backend <-> JWT cookie | HTTP cookie | Backend | `refresh_token` path/samesite/secure |

## Критичные интеграционные сценарии

- login + refresh + logout;
- привязка полиса и загрузка кабинета пациента;
- поиск врача, слоты, запись, перенос, отмена;
- просмотр расписания врача и талона;
- CSV import/export для администратора.

## Контроль консистентности

- API-контракты и docs обновляются вместе;
- frontend и backend используют одну role-matrix;
- runtime JSON соответствует `snake_case`.
