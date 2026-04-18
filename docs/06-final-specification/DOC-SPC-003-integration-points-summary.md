# DOC-SPC-003 Сводка точек интеграции

| Версия | Статус | Дата обновления |
|---|---|---|
| v1.1 | Draft | 2026-04-18 |

## Точки интеграции

1. Frontend <-> Backend API (`/api/v1/**`)
2. Backend <-> PostgreSQL
3. Nginx <-> Backend
4. Nginx <-> Frontend

## Ключевые API-контуры

- Auth: access token в JSON, refresh token в HttpOnly cookie;
- Users/Patients: профиль, пароль, полис, админский CRUD;
- Doctors: поиск, public/private slots, calendar, schedule;
- Appointments: create/update/cancel/detail;
- Admins: CSV import/export.

## Матрица интеграционных точек

| Точка | Протокол | Владелец | Основной риск |
|---|---|---|---|
| Browser <-> Nginx | HTTP/HTTPS | DevOps | Ошибка маршрутизации `/api` и SPA routes |
| Nginx <-> Frontend | HTTP (internal) | Frontend + DevOps | Отдача устаревшей сборки/неверный base path |
| Nginx <-> Backend | HTTP (internal) | Backend + DevOps | 502/504 при недоступности backend |
| Frontend <-> Backend API | HTTP/JSON | Frontend + Backend | Несовместимость DTO/ролей/CORS |
| Backend <-> PostgreSQL | TCP/JDBC | Backend | Ошибки схемы и миграций |

## Критичные интеграционные сценарии

- login + refresh + logout;
- получение профиля и смена пароля;
- получение врачей и слотов, включая public endpoints;
- создание/отмена/обновление записи;
- CSV export/import для админа.

## Контроль консистентности

- API-контракты и docs обновляются в одном PR;
- frontend и backend используют единую role-matrix.
