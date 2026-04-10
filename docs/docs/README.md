# RegistrationOffice - Карта документации

| Версия | Статус | Дата создания | Дата обновления |
|---|---|---|---|
| v1.0 | Draft | 2026-04-01 | 2026-04-01 |

О документе: навигация по документам проекта RegistrationOffice.

Для кого: команда backend/frontend, аналитик, тестировщик, заказчик.

## Контекст проекта

`RegistrationOffice` развивается как fullstack-решение:
- `backend` (Spring Boot + PostgreSQL + JWT);
- `frontend` (веб-клиент для пациентов, врачей, регистратора и администратора);
- инфраструктура деплоя через Docker Compose и Nginx;
- отдельные контейнеры для `backend` и `frontend`.

## Структура

- `00-governance` - устав, риски, команда, ретро
- `01-discovery` - предметная область и персоны
- `02-requirements` - user stories и story map
- `03-planning` - реализуемость и план работ
- `04-architecture` - процессы, контекст, wireframes
- `05-data-and-integration` - ERD, словарь данных, API-контракты
- `06-final-specification` - финальная спецификация решения
- `07-validation-and-delivery` - тестирование, демо, деплой и передача
- `90-checkpoints` - срезы CP1-CP4
- `98-source-materials` - исходные материалы
- `99-standards` - правила оформления документации

## Источники истины в коде

- API и безопасность: `backend/src/main/java/ru/katacan/registrationoffice`
- Конфиги: `backend/src/main/resources/application.properties`
- Контейнеризация: `backend/Dockerfile`, `frontend/Dockerfile`, `frontend/nginx/default.conf`, `docker-compose.yml`, `deploy/nginx/default.conf`

## Правило синхронизации

При изменении API/ролей/модели данных обновляются минимум:
1. `README.md` (корень)
2. `docs/docs/05-data-and-integration/*`
3. `docs/docs/06-final-specification/*`
4. `docs/docs/90-checkpoints/*`
