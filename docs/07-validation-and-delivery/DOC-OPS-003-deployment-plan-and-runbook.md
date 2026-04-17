# DOC-OPS-003 — План развёртывания и runbook

| Версия | Статус | Дата создания | Дата обновления |
|--------|--------|---------------|-----------------|
| v0.1   | Draft  | 2026-04-10    | 2026-04-10      |

О документе: инструкция по развёртыванию и запуску системы RegistrationOffice.

Для кого: backend-разработчик, DevOps, команда проекта.

Основано на: репозиторий проекта, `Dockerfile`, `docker-compose.yml`, `deploy/nginx/default.conf`.

---

## Авторы документа

- Сысоев Георгий — backend developer — деплой
- Чернышов Ярослав — backend developer — Docker-конфигурация

## История изменений

- [v0.1] [2026-04-10] (Сысоев, Чернышов): первичный runbook.

---

## Архитектура деплоя
| Версия | Статус | Дата обновления |
|---|---|---|
| v1.1 | Draft | 2026-04-17 |

```
Internet → Nginx (reverse proxy, :80) → Spring Boot API (:8080) → PostgreSQL (:5432)
                                    ↘ Static files (frontend)
```

Все компоненты запускаются через Docker Compose на одной виртуальной машине в Yandex Cloud.
- Linux VM
- Docker + Docker Compose
- Nginx + backend + frontend + postgres

## Предусловия

- Виртуальная машина с Ubuntu 22.04+.
- Установлены Docker и Docker Compose.
- Домен registration-office.ru направлен на IP машины.
- Обязательные: `postgres`, `backend`, `frontend`, `nginx`
- По текущему `docker-compose.yml` frontend собирается отдельным контейнером и отдается через `nginx`.

## Шаги развёртывания

### 1. Клонировать репозиторий

```bash
git clone https://gitverse.ru/KatacanXD/RegistrationOffice.git
cd RegistrationOffice
```

### 2. Настроить переменные окружения
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

```bash
cp .env.example .env
# Отредактировать .env:
# - POSTGRES_PASSWORD
# - JWT_SECRET
# - SPRING_DATASOURCE_URL
```

### 3. Собрать и запустить
- главная страница UI отдается по `/`;
- SPA-маршруты не дают `404` при прямом открытии URL;
- запросы frontend к `/api/v1/**` проходят через `nginx`;
- login из UI получает access token, а refresh token сохраняется в cookie;
- logout очищает `refresh_token` cookie.

```bash
docker-compose up -d --build
```

- `404` на SPA-роутах: проверить fallback на `index.html` в `nginx`.
- `CORS` ошибки в браузере: проверить `APP_SECURITY_CORS_ALLOWED_ORIGINS` и origin frontend.
- UI работает, API нет: проверить проксирование `/api` в `deploy/nginx/default.conf`.
- После деплоя старая версия UI: проверить пересборку frontend-образа и кэш браузера/CDN.
- `refresh` не работает: проверить cookie path `/api/v1/auth`, `Secure`, `SameSite=Lax` и домен.
### 4. Проверить работоспособность

```bash
# Проверить статус контейнеров
docker-compose ps

# Проверить API
curl http://localhost:8080/api/doctors

# Проверить через домен
curl http://registration-office.ru/api/doctors
```

### 5. Загрузить начальные данные (при необходимости)

```bash
# Импорт врачей, пациентов, слотов через CSV или SQL-скрипты
# (описано в документации к API: POST /api/work-slots/import-csv)
```

## Обновление

```bash
cd RegistrationOffice
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
# Логи приложения
docker-compose logs -f app

# Логи Nginx
docker-compose logs -f nginx

# Логи PostgreSQL
docker-compose logs -f db
```

## Известные ограничения

- HTTPS не настроен (учебный проект).
- Backup БД не автоматизирован.
- Масштабирование на несколько инстансов не предусмотрено.
