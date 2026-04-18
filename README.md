### Ссылка на тестовый сайт: https://registration-office.ru
### Ссылка на Kaiten: https://myoprlove.yougile.com/team/efcc60ee0048/EMIAS-2.0?lang=ru
### Ссылка на Figma: https://www.figma.com/design/dhSpD9QDRP79l77V0HJsWv/%D0%9E%D0%9F%D0%94?node-id=0-1&p=f&t=QOJygsnFG9sZETfO-0


# RegistrationOffice - Карта документации

| Версия | Статус | Дата создания | Дата обновления |
|--------|---|---|---|
| v0.2   | Draft | 2026-04-01 | 2026-04-17 |

О документе: навигация по документам проекта RegistrationOffice.

Для кого: команда backend/frontend, аналитик, тестировщик, заказчик.

## Контекст проекта

`RegistrationOffice` развивается как fullstack-решение:
- `backend` (Spring Boot + PostgreSQL + JWT + CSV import/export);
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

- API-контроллеры: `backend/src/main/java/ru/katacan/registrationoffice/controller`
- Security и доступы: `backend/src/main/java/ru/katacan/registrationoffice/config/SecurityConfig.java`
- Модель данных (JPA сущности): `backend/src/main/java/ru/katacan/registrationoffice/entity`
- DTO и мапперы: `backend/src/main/java/ru/katacan/registrationoffice/dto`, `backend/src/main/java/ru/katacan/registrationoffice/mapper`
- Конфигурация приложения: `backend/src/main/resources/application.properties`
- Контейнеризация и прокси: `backend/Dockerfile`, `frontend/Dockerfile`, `frontend/nginx/default.conf`, `docker-compose.yml`, `deploy/nginx/default.conf`
- Frontend-контур: отдельный контейнер со статической сборкой Vite и proxy через `/api`

## Правило синхронизации

При изменении API/ролей/модели данных/контейнерного контура обновляются минимум:
1. `README.md` (корень)
2. `docs/05-data-and-integration/*`
3. `docs/06-final-specification/*`
4. `docs/07-validation-and-delivery/*`
5. `docs/90-checkpoints/*`
