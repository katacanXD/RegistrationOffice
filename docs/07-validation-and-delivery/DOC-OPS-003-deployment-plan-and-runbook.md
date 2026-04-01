# DOC-OPS-003 План деплоя и runbook

| Версия | Статус | Дата обновления |
|---|---|---|
| v1.0 | Draft | 2026-04-01 |

## Целевая среда

- Linux VM
- Docker + Docker Compose
- Nginx + backend + postgres (+ frontend container, если используется отдельный сервис)

## Состав сервисов

- Обязательные: `postgres`, `backend`, `nginx`
- Опционально: `frontend` (если фронт запускается отдельным контейнером, а не как статика в `nginx`)
- По текущему `docker-compose.yml` в репозитории описаны `postgres`, `backend`, `nginx`; frontend-контур документируется как целевой fullstack-вариант.

## Шаги

1. Скопировать проект на VM.
2. Создать `.env` из `.env.example`.
3. Запустить `docker compose up -d --build`.
4. Проверить логи backend/nginx.

## Проверка после деплоя

- доступен `POST /api/v1/auth/login`;
- backend контейнер в `healthy/running`;
- postgres принимает подключения.

## Проверки frontend

- главная страница UI отдается по `/`;
- SPA-маршруты не дают `404` при прямом открытии URL;
- запросы frontend к `/api/v1/**` проходят через `nginx`;
- login из UI получает токен и открывает роль-специфичный экран.

## Типовые сбои frontend-контура

- `404` на SPA-роутах: проверить fallback на `index.html` в `nginx`.
- `CORS` ошибки в браузере: проверить `APP_SECURITY_CORS_ALLOWED_ORIGINS` и origin frontend.
- UI работает, API нет: проверить проксирование `/api` в `deploy/nginx/default.conf`.
- После деплоя старая версия UI: проверить пересборку frontend-образа и кэш браузера/CDN.

## Операционные команды

- `docker compose ps`
- `docker compose logs --tail 200 backend`
- `docker compose up -d --build backend`
- `docker compose down`
