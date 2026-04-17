# DOC-OPS-003 План деплоя и runbook

| Версия | Статус | Дата обновления |
|---|---|---|
| v1.1 | Draft | 2026-04-17 |

## Целевая среда

- Linux VM
- Docker + Docker Compose
- Nginx + backend + frontend + postgres

## Состав сервисов

- Обязательные: `postgres`, `backend`, `frontend`, `nginx`
- По текущему `docker-compose.yml` frontend собирается отдельным контейнером и отдается через `nginx`.

## Шаги

1. Скопировать проект на VM.
2. Создать `.env` из `.env.example`.
3. Запустить `docker compose up -d --build`.
4. Проверить логи backend/nginx.

## Проверка после деплоя

- доступен `POST /api/v1/auth/login`;
- backend контейнер в `running`;
- postgres принимает подключения;
- frontend доступен по `/` и открывает SPA без 404 на маршрутах.

## Переменные окружения

Минимальный набор для `.env`:

```dotenv
DB_NAME=postgres
DB_USER=postgres
DB_PASS=<strong_password>
SECURITY_JWT_SECRET=<jwt_secret_min_32_chars>
APP_SECURITY_CORS_ALLOWED_ORIGINS=
APP_SECURITY_CORS_ALLOWED_ORIGIN_PATTERNS=
VITE_API_BASE_URL=http://<url>/api/v1
```

Примечания:

- `VITE_API_BASE_URL` используется frontend при сборке; если не задан, клиент по умолчанию ходит на `/api/v1`.
- cookie `refresh_token` работает только при корректном домене/HTTPS и `SameSite`/`Secure` настройках.

## Проверки frontend

- главная страница UI отдается по `/`;
- SPA-маршруты не дают `404` при прямом открытии URL;
- запросы frontend к `/api/v1/**` проходят через `nginx`;
- login из UI получает access token, а refresh token сохраняется в cookie;
- logout очищает `refresh_token` cookie.

## Типовые сбои frontend-контура

- `404` на SPA-роутах: проверить fallback на `index.html` в `nginx`.
- `CORS` ошибки в браузере: проверить `APP_SECURITY_CORS_ALLOWED_ORIGINS` и origin frontend.
- UI работает, API нет: проверить проксирование `/api` в `deploy/nginx/default.conf`.
- После деплоя старая версия UI: проверить пересборку frontend-образа и кэш браузера/CDN.
- `refresh` не работает: проверить cookie path `/api/v1/auth`, `Secure`, `SameSite=Lax` и домен.

## Операционные команды

- `docker compose ps`
- `docker compose logs --tail 200 backend`
- `docker compose up -d --build backend`
- `docker compose down`
