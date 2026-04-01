# DOC-SPC-003 Сводка точек интеграции

| Версия | Статус | Дата обновления |
|---|---|---|
| v1.0 | Draft | 2026-04-01 |

## Точки интеграции

1. Frontend <-> Backend API (`/api/v1/**`)
2. Backend <-> PostgreSQL
3. Nginx <-> Backend
4. Nginx <-> Frontend

## Матрица интеграционных точек

| Точка | Протокол | Владелец | Основной риск |
|---|---|---|---|
| Browser <-> Nginx | HTTP/HTTPS | DevOps | Ошибка маршрутизации `/api` и SPA routes |
| Nginx <-> Frontend | HTTP (internal) | Frontend + DevOps | Отдача устаревшей сборки/неверный base path |
| Nginx <-> Backend | HTTP (internal) | Backend + DevOps | 502/504 при недоступности backend |
| Frontend <-> Backend API | HTTP/JSON | Frontend + Backend | Несовместимость DTO/ролей/CORS |
| Backend <-> PostgreSQL | TCP/JDBC | Backend | Ошибки схемы и миграций |

## Критичные интеграционные сценарии

- login + refresh;
- получение профиля;
- получение врачей и слотов;
- создание/отмена/обновление записи.

## Контроль консистентности

- API-контракты и docs обновляются в одном PR;
- frontend и backend используют единую role-matrix.
