# RegistrationOffice

## Система управления графиком приёмов врача

Сервис решает проблему «окон» в расписании врачей, двойного бронирования и отсутствия прозрачной отчётноcти. Пациенты записываются на приём через веб-интерфейс, регистратура управляет записями всех врачей, администратор видит загрузку и экспортирует отчёты в CSV.

**Демо:** https://registration-office.ru  
**Ссылка на Kaiten:** https://myoprlove.yougile.com/team/efcc60ee0048/EMIAS-2.0?lang=ru  
**Ссылка на Figma:** https://www.figma.com/design/dhSpD9QDRP79l77V0HJsWv/%D0%9E%D0%9F%D0%94?node-id=0-1&p=f&t=QOJygsnFG9sZETfO-0

---

## Команда

| ФИО | Группа | Роль | Логин GitVerse |
|---|---|---|----------------|
| Наботов Карим | ЭФБО-10-25 | Project Lead |                |
| Лукьянов Илья | ЭФБО-10-25 | Frontend developer |                |
| Чернышов Ярослав | ЭФБО-10-25 | Backend developer | KatacanXD      |
| Сысоев Георгий | ЭФБО-10-25 | Backend developer |                |
| Романчук Даниил | ЭФБО-10-25 | Designer / Tester |                |

> Логины участников заполняются каждым лично. Карим — владелец репозитория.

**Заказчик:** Сивцев Алексей Олегович.

---

## Что умеет система

- **Расписание:** администратор задаёт рабочие интервалы врачей (дата, время, длительность слота, перерывы). Слоты генерируются автоматически.
- **Запись:** пациент или регистратор выбирает врача, дату и свободный слот. Система проверяет 5 валидаций (врач существует, пациент существует, слот в интервале, не перерыв, не занят).
- **Перенос/отмена:** запись можно перенести на другой свободный слот или отменить с указанием причины.
- **Отчёт:** утилизация расписания по врачу/дате/специальности. Экспорт CSV.
- **Роли:** пациент, врач, регистратура, администратор. JWT-аутентификация.

---

## Макеты и сценарии

Дизайн-макеты всех экранов, интерактивные прототипы и анимации пользовательских сценариев собраны в Figma.

Описание экранной логики и привязка к API — в [DOC-ARC-003](docs/04-architecture/DOC-ARC-003-low-fi-wireframes-and-screen-logic.md), описание анимаций и структура Figma-проекта — в [DOC-ARC-004](docs/04-architecture/DOC-ARC-004-figma-wireframe-generation.md).

---

## Текущие источники истины в коде

- API-контроллеры: `backend/src/main/java/ru/katacan/registrationoffice/controller`
- Security и доступы: `backend/src/main/java/ru/katacan/registrationoffice/config/SecurityConfig.java`
- Модель данных (JPA сущности): `backend/src/main/java/ru/katacan/registrationoffice/entity`
- DTO и мапперы: `backend/src/main/java/ru/katacan/registrationoffice/dto`, `backend/src/main/java/ru/katacan/registrationoffice/mapper`
- Конфигурация приложения: `backend/src/main/resources/application.properties`
- Контейнеризация и прокси: `backend/Dockerfile`, `frontend/Dockerfile`, `frontend/nginx/default.conf`, `docker-compose.yml`, `deploy/nginx/default.conf`

## Что важно помнить про backend

- все публичные JSON-поля идут в `snake_case`;
- refresh token передается и обновляется через `HttpOnly` cookie `refresh_token`, а не в теле ответа;
- frontend и backend разворачиваются отдельными контейнерами, а `nginx` проксирует `/api/v1/**` и отдает SPA.

---

## Стек

| Слой | Технология |
|---|---|
| Backend | Spring Boot (Java) |
| Frontend | React (JS), HTML, CSS |
| БД | PostgreSQL |
| Auth | JWT |
| Инфраструктура | Docker Compose, Nginx |
| Деплой | Yandex Cloud |

---

## Как запустить

```bash
git clone https://gitverse.ru/KatacanXD/RegistrationOffice.git
cd RegistrationOffice
cp .env.example .env
docker-compose up -d --build
curl http://localhost:8080/api/v1/auth/me
```

---

## Документация

Вся проектная документация лежит в `docs/`. Навигация — в [docs/README.md](docs/README.md).

### Что внутри

| Документ | Где лежит |
|---|---|
| Карточка проекта | [DOC-GOV-001](docs/00-governance/DOC-GOV-001-project-charter.md) |
| Матрица рисков | [DOC-GOV-002](docs/00-governance/DOC-GOV-002-risk-register-and-matrix.md) |
| Роли — кто что делает | [DOC-GOV-003](docs/00-governance/DOC-GOV-003-team-roles-and-communication-plan.md) |
| Календарный план (WBS) | [DOC-PLN-002](docs/03-planning/DOC-PLN-002-wbs-and-work-plan.md) |
| Предметная область | [DOC-DIS-001](docs/01-discovery/DOC-DIS-001-domain-research-menu-engineering.md) |
| Персоны пользователей | [DOC-DIS-002](docs/01-discovery/DOC-DIS-002-personas.md) |
| User Stories | [DOC-REQ-001](docs/02-requirements/DOC-REQ-001-user-stories.md) |
| User Story Map | [DOC-REQ-002](docs/02-requirements/DOC-REQ-002-user-story-map.md) |
| BPMN — бизнес-процессы | [DOC-ARC-001](docs/04-architecture/DOC-ARC-001-business-process-and-user-flow.md) |
| Архитектура + границы MVP | [DOC-ARC-002](docs/04-architecture/DOC-ARC-002-system-context-and-solution-boundaries.md) |
| Low-fi wireframes | [DOC-ARC-003](docs/04-architecture/DOC-ARC-003-low-fi-wireframes-and-screen-logic.md) |
| Figma: макеты и анимации | [DOC-ARC-004](docs/04-architecture/DOC-ARC-004-figma-wireframe-generation.md) |
| ER-диаграмма | [DOC-DAT-001](docs/05-data-and-integration/DOC-DAT-001-logical-data-model-and-erd.md) |
| Словарь данных | [DOC-DAT-002](docs/05-data-and-integration/DOC-DAT-002-data-dictionary-and-validation-rules.md) |
| REST API | [DOC-DAT-003](docs/05-data-and-integration/DOC-DAT-003-api-specification.md) |
| DFD | [DOC-DAT-004](docs/05-data-and-integration/DOC-DAT-004.md) |
| Маппинг данных | [DOC-DAT-005](docs/05-data-and-integration/DAT-005-data-mapping.md) |
| SQL-запросы к БД | [DOC-DAT-006](docs/05-data-and-integration/DAT-006-sql-queries.md) |
| Финальная спецификация | [DOC-SPC-001](docs/06-final-specification/DOC-SPC-001-final-technical-specification.md) |
| Архитектура решения | [DOC-SPC-002](docs/06-final-specification/DOC-SPC-002-solution-architecture-and-module-map.md) |
| Сводка точек интеграции | [DOC-SPC-003](docs/06-final-specification/DOC-SPC-003-integration-points-summary.md) |
| Тест-кейсы | [DOC-TST-001](docs/07-validation-and-delivery/DOC-TST-001-acceptance-test-scenarios.md) |
| Демо-сценарий | [DOC-OPS-001](docs/07-validation-and-delivery/DOC-OPS-001-demo-scenario-and-walkthrough.md) |
| План пилота и передачи | [DOC-OPS-002](docs/07-validation-and-delivery/DOC-OPS-002-pilot-rollout-and-handover-plan.md) |
| Деплой (runbook) | [DOC-OPS-003](docs/07-validation-and-delivery/DOC-OPS-003-deployment-plan-and-runbook.md) |

### Контрольные точки

| CP | Дата | Что сделано |
|---|---|---|
| [CP1](docs/90-checkpoints/CP1-deliverables.md) | 16.03 | Устав, персоны, user stories, риски, WBS |
| [CP2](docs/90-checkpoints/CP2-deliverables.md) | 06.04 | BPMN, контекст, wireframes |
| [CP3](docs/90-checkpoints/CP3-deliverables.md) | 20.04 | ERD, API, DFD, SQL, маппинг |
| [CP4](docs/90-checkpoints/CP4-deliverables.md) | 01.05 | Финальное ТЗ, тесты, демо, деплой |

---

## Структура репозитория

```
RegistrationOffice/
├── docs/
├── backend/
├── frontend/
├── deploy/
├── docker-compose.yml
└── README.md
```
