# DOC-OPS-003 План деплоя и runbook

| Версия | Статус | Дата обновления |
|---|---|---|
| v1.0 | Draft | 2026-04-01 |

## Целевая среда

- Linux VM
- Docker + Docker Compose
- Nginx + backend + frontend + postgres

## Шаги

1. Скопировать проект на VM.
2. Создать `.env` из `.env.example`.
3. Запустить `docker compose up -d --build`.
4. Проверить логи backend/frontend/nginx.

## Проверка после деплоя

- доступен `POST /api/v1/auth/login`;
- backend контейнер в `healthy/running`;
- postgres принимает подключения.

## Операционные команды

- `docker compose ps`
- `docker compose logs --tail 200 backend`
- `docker compose logs --tail 200 frontend`
- `docker compose up -d --build backend`
- `docker compose up -d --build backend frontend nginx`
- `docker compose down`
