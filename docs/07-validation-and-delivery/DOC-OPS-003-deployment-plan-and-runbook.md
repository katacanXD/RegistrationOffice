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

```
Internet → Nginx (reverse proxy, :80) → Spring Boot API (:8080) → PostgreSQL (:5432)
                                    ↘ Static files (frontend)
```

Все компоненты запускаются через Docker Compose на одной виртуальной машине в Yandex Cloud.

## Предусловия

- Виртуальная машина с Ubuntu 22.04+.
- Установлены Docker и Docker Compose.
- Домен registration-office.ru направлен на IP машины.

## Шаги развёртывания

### 1. Клонировать репозиторий

```bash
git clone https://gitverse.ru/KatacanXD/RegistrationOffice.git
cd RegistrationOffice
```

### 2. Настроить переменные окружения

```bash
cp .env.example .env
# Отредактировать .env:
# - POSTGRES_PASSWORD
# - JWT_SECRET
# - SPRING_DATASOURCE_URL
```

### 3. Собрать и запустить

```bash
docker-compose up -d --build
```

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
