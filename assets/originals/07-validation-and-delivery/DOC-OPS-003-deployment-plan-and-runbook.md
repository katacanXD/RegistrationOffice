# DOC-OPS-003 — План развёртывания и runbook

| Версия | Статус | Дата создания | Дата обновления |
|--------|--------|---------------|-----------------|
| v0.2   | Draft  | 2026-04-10    | 2026-04-18      |

О документе: инструкция по развёртыванию и проверке текущего RegistrationOffice.

Для кого: backend-разработчик, DevOps, команда проекта.

Основано на: репозиторий проекта, `Dockerfile`, `docker-compose.yml`, `deploy/nginx/default.conf`.

---

## Авторы документа

- Сысоев Георгий — backend developer — деплой
- Чернышов Ярослав — backend developer — Docker-конфигурация

## История изменений

- [v0.1] [2026-04-10] (Сысоев, Чернышов): первичный runbook.
- [v0.2] [2026-04-18] (Чернышов): добавлены проверки и обновление.

---

## Архитектура деплоя

```
Browser → Nginx → Frontend SPA
                → Spring Boot API → PostgreSQL
```

Все компоненты запускаются через Docker Compose на одной VM в Yandex Cloud.

## Предусловия

- Ubuntu VM с Docker и Docker Compose.
- Домен/host указывает на VM.
- Наличие `.env` с параметрами БД и JWT.

## Переменные окружения

```dotenv
DB_NAME=postgres
DB_USER=postgres
DB_PASS=<strong_password>
SECURITY_JWT_SECRET=<jwt_secret_min_32_chars>
APP_SECURITY_CORS_ALLOWED_ORIGINS=
APP_SECURITY_CORS_ALLOWED_ORIGIN_PATTERNS=
VITE_API_BASE_URL=http://<host>/api/v1
```

## Шаги развёртывания

1. Склонировать репозиторий.
2. Подготовить `.env`.
3. Выполнить `docker-compose up -d --build`.
4. Проверить, что SPA доступна по `/` и маршруты не дают `404`.
5. Проверить `/api/v1/auth/login`, `/api/v1/doctors`, `/api/v1/patients/me/dashboard`.
6. Убедиться, что `refresh_token` cookie устанавливается и обновляется.

## Проверки после деплоя

- `docker-compose ps`
- `curl http://localhost:8080/api/v1/doctors`
- `curl http://<host>/api/v1/doctors`
- вход через UI с последующим refresh/logout

## Обновление

```bash
git pull
docker-compose up -d --build
```

## Откат

```bash
docker-compose down
git checkout <предыдущий-коммит>
docker-compose up -d --build
```

## Мониторинг

```bash
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f nginx
docker-compose logs -f db
```

## Известные ограничения

- HTTPS может быть не настроен в учебной среде.
- Backup БД не автоматизирован.
- Масштабирование на несколько инстансов не предусмотрено.
