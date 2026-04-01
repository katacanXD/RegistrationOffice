# Ссылка на тестовый сайт: http://registration-office.ru/api

# RegistrationOffice - Карта документации

| Версия | Статус | Дата создания | Дата обновления |
|--------|---|---|---|
| v0.1   | Draft | 2026-04-01 | 2026-04-01 |

О документе: навигация по документам проекта RegistrationOffice.

Для кого: команда backend/frontend, аналитик, тестировщик, заказчик.

## Контекст проекта

`RegistrationOffice` развивается как fullstack-решение:
- `backend` (Spring Boot + PostgreSQL + JWT);
- `frontend` (веб-клиент для пациентов, врачей, регистратора и администратора);
- инфраструктура деплоя через Docker Compose и Nginx.

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

## Текущие источники истины в коде

При обновлении документации ориентируйтесь на:

- API-контроллеры: `src/main/java/ru/katacan/registrationoffice/controller`
- Security и доступы: `src/main/java/ru/katacan/registrationoffice/config/SecurityConfig.java`
- Модель данных (JPA сущности): `src/main/java/ru/katacan/registrationoffice/entity`
- Конфигурация приложения: `src/main/resources/application.properties`
- Контейнеризация и прокси: `Dockerfile`, `docker-compose.yml`, `deploy/nginx/default.conf`
- Frontend-контур (если вынесен в отдельный контейнер): `frontend Dockerfile`, compose-конфиг, переменные `VITE_*`

## Правило синхронизации

При изменении API/ролей/модели данных/контейнерного контура обновляются минимум:
1. `README.md` (корень)
2. `docs/docs/05-data-and-integration/*`
3. `docs/docs/06-final-specification/*`
4. `docs/docs/07-validation-and-delivery/*`
5. `docs/docs/90-checkpoints/*`
