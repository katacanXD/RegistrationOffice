# RegistrationOffice

## Система управления графиком приёмов врача

Сервис решает проблему «окон» в расписании врачей, двойного бронирования и отсутствия прозрачной отчётности. Пациенты записываются на приём через веб-интерфейс, регистратура управляет записями всех врачей, администратор видит загрузку и экспортирует отчёты в CSV.

**Демо:** http://registration-office.ru
**Ссылка на Kaiten:** https://myoprlove.yougile.com/team/efcc60ee0048/EMIAS-2.0?lang=ru
**Ссылка на Figma:** https://www.figma.com/design/dhSpD9QDRP79l77V0HJsWv/%D0%9E%D0%9F%D0%94?node-id=0-1&p=f&t=QOJygsnFG9sZETfO-0


---

## Команда

| ФИО                  | Группа     | Роль                  | Логин GitVerse  |
|----------------------|------------|-----------------------|-----------------|
| Наботов Карим        | ЭФБО-10-25 | Project Lead          | KatacanXD       |
| Лукьянов Илья        | ЭФБО-10-25 | Frontend developer    |                 |
| Чернышов Ярослав     | ЭФБО-10-25 | Backend developer     |                 |
| Сысоев Георгий       | ЭФБО-10-25 | Backend developer     |                 |
| Романчук Даниил      | ЭФБО-10-25 | Designer / Tester     |                 |

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

Дизайн-макеты всех экранов, интерактивные прототипы и анимации пользовательских сценариев собраны в Figma:

**[Открыть в Figma](https://www.figma.com/design/dhSpD9QDRP79l77V0HJsWv/%D0%9E%D0%9F%D0%94?node-id=0-1&p=f&t=QOJygsnFG9sZETfO-0)**

Внутри — 10 экранов (логин, выбор врача, календарь слотов, подтверждение записи, мои записи, расписание врача, управление записями, настройка расписания, отчёт утилизации, импорт CSV) и 7 кликабельных прототипов по каждому пользовательскому сценарию: запись, отмена, перенос, просмотр расписания врачом, запись через регистратуру, отчёт и импорт CSV.

Описание экранной логики и привязка к API — в [DOC-ARC-003](docs/04-architecture/DOC-ARC-003-low-fi-wireframes.md), описание анимаций и структура Figma-проекта — в [DOC-ARC-004](docs/04-architecture/DOC-ARC-004-figma-wireframes.md).

---

## Макеты и сценарии

Все дизайн-макеты экранов, интерактивные прототипы и анимации пользовательских сценариев — в Figma:

**[Открыть в Figma](https://www.figma.com/design/dhSpD9QDRP79l77V0HJsWv/%D0%9E%D0%9F%D0%94?node-id=0-1&p=f&t=QOJygsnFG9sZETfO-0)**

Внутри: макеты каждого экрана по ролям (пациент, врач, регистратура, администратор), кликабельные прототипы основных сценариев (запись, отмена, перенос, просмотр расписания, отчёт утилизации, импорт CSV) и анимации переходов.

---

## Стек

| Слой           | Технология                   |
|----------------|------------------------------|
| Backend        | Spring Boot (Java)           |
| Frontend       | React (JS), HTML, CSS        |
| БД             | PostgreSQL                   |
| Auth           | JWT                          |
| Инфраструктура | Docker Compose, Nginx        |
| Деплой         | Yandex Cloud                 |

---

## Как запустить

```bash
git clone https://gitverse.ru/KatacanXD/RegistrationOffice.git
cd RegistrationOffice
cp .env.example .env        # заполнить пароли
docker-compose up -d --build
curl http://localhost:8080/api/doctors
```

---

## Документация

Вся проектная документация лежит в `docs/`. Навигация — в [docs/README.md](docs/README.md).

### Что внутри

| Документ                          | Где лежит                                         |
|-----------------------------------|----------------------------------------------------|
| Карточка проекта                  | [DOC-GOV-001](docs/00-governance/DOC-GOV-001-project-charter.md)       |
| Матрица рисков                    | [DOC-GOV-002](docs/00-governance/DOC-GOV-002-risk-matrix.md)           |
| Роли — кто что делает             | [DOC-GOV-003](docs/00-governance/DOC-GOV-003-team-rules.md)            |
| Календарный план (WBS)            | [DOC-GOV-004](docs/00-governance/DOC-GOV-004-wbs.md)                   |
| Предметная область                | [DOC-DSC-001](docs/01-discovery/DOC-DSC-001-domain-research.md)        |
| Персоны пользователей             | [DOC-DSC-002](docs/01-discovery/DOC-DSC-002-personas.md)               |
| User Stories                      | [DOC-REQ-001](docs/02-requirements/DOC-REQ-001-user-stories.md)        |
| User Story Map                    | [DOC-REQ-002](docs/02-requirements/DOC-REQ-002-user-story-map.md)      |
| UX-сценарии всех ролей            | [DOC-REQ-003](docs/02-requirements/DOC-REQ-003-ux-scenarios.md)        |
| Оценка реализуемости              | [DOC-PLN-001](docs/03-planning/DOC-PLN-001-feasibility.md)             |
| Фич-лист (по часам и разработчикам)| [DOC-PLN-002](docs/03-planning/DOC-PLN-002-feature-list.md)            |
| BPMN — бизнес-процессы            | [DOC-ARC-001](docs/04-architecture/DOC-ARC-001-bpmn-processes.md)      |
| Архитектура + границы MVP          | [DOC-ARC-002](docs/04-architecture/DOC-ARC-002-system-context.md)      |
| Low-fi wireframes (экранная логика)| [DOC-ARC-003](docs/04-architecture/DOC-ARC-003-low-fi-wireframes.md)   |
| Figma: макеты и анимации           | [DOC-ARC-004](docs/04-architecture/DOC-ARC-004-figma-wireframes.md)    |
| ER-диаграмма                      | [DOC-DAT-001](docs/05-data-and-integration/DOC-DAT-001-erd.md)         |
| Словарь данных                    | [DOC-DAT-002](docs/05-data-and-integration/DOC-DAT-002-data-dictionary.md)|
| REST API (GET/POST/PUT)           | [DOC-DAT-003](docs/05-data-and-integration/DOC-DAT-003-api-specification.md)|
| DFD                               | [DOC-DAT-004](docs/05-data-and-integration/DOC-DAT-004-dfd.md)         |
| Маппинг данных                    | [DOC-DAT-005](docs/05-data-and-integration/DOC-DAT-005-data-mapping.md) |
| SQL-запросы к БД (6 шт.)          | [DOC-DAT-006](docs/05-data-and-integration/DOC-DAT-006-sql-queries.md)  |
| Финальная спецификация            | [DOC-SPC-001](docs/06-final-specification/DOC-SPC-001-final-specification.md)|
| Архитектура решения               | [DOC-SPC-002](docs/06-final-specification/DOC-SPC-002-solution-architecture.md)|
| Сводка точек интеграции           | [DOC-SPC-003](docs/06-final-specification/DOC-SPC-003-integration-points.md)|
| Тест-кейсы (14 шт.)              | [DOC-OPS-001](docs/07-validation-and-delivery/DOC-OPS-001-test-scenarios.md)|
| Демо-сценарий                     | [DOC-OPS-002](docs/07-validation-and-delivery/DOC-OPS-002-demo-scenario.md)|
| Деплой (runbook)                  | [DOC-OPS-003](docs/07-validation-and-delivery/DOC-OPS-003-deployment-plan.md)|

### Контрольные точки

| CP  | Дата       | Что сделано                                    |
|-----|------------|------------------------------------------------|
| [CP1](docs/90-checkpoints/CP1-deliverables.md) | 16.03 | Устав, персоны, user stories, риски, WBS |
| [CP2](docs/90-checkpoints/CP2-deliverables.md) | 06.04 | BPMN, Event Storming, контекст, wireframes |
| [CP3](docs/90-checkpoints/CP3-deliverables.md) | 20.04 | ERD, API, DFD, SQL-запросы, маппинг |
| [CP4](docs/90-checkpoints/CP4-deliverables.md) | 01.05 | Финальное ТЗ, тесты, демо, деплой |

---

## Структура репозитория

```
RegistrationOffice/
├── docs/                           # Документация (22 документа)
│   ├── 00-governance/              # Карточка, риски, роли, WBS
│   ├── 01-discovery/               # Предметная область, персоны
│   ├── 02-requirements/            # User stories, UX-сценарии
│   ├── 03-planning/                # Реализуемость, фич-лист
│   ├── 04-architecture/            # BPMN, архитектура
│   ├── 05-data-and-integration/    # ERD, API, DFD, SQL, маппинг
│   ├── 06-final-specification/     # Финальное ТЗ
│   ├── 07-validation-and-delivery/ # Тесты, демо, деплой
│   ├── 90-checkpoints/             # CP1–CP4
│   └── 98-source-materials/        # Исходный кейс
├── src/                            # Spring Boot (Java)
├── deploy/                         # Nginx
├── Dockerfile
├── docker-compose.yml
└── README.md                       # Этот файл
```
