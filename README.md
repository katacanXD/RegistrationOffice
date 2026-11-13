### Ссылка на тестовый сайт: https://registration-office.ru
### Ссылка на Kaiten: https://myoprlove.yougile.com/team/efcc60ee0048/EMIAS-2.0?lang=ru
### Ссылка на Figma: https://www.figma.com/design/dhSpD9QDRP79l77V0HJsWv/%D0%9E%D0%9F%D0%94?node-id=0-1&p=f&t=QOJygsnFG9sZETfO-0

# RegistrationOffice — сервис записи на приём

| Версия | Статус | Дата создания | Дата обновления |
|--------|--------|---------------|-----------------|
| v0.3   | Actual | 2026-04-01    | 2026-11-07      |

`RegistrationOffice` — fullstack-решение для записи пациентов на приём:
- `backend` — Spring Boot + PostgreSQL + JWT + CSV import/export;
- `frontend` — React SPA для пациента, врача, регистратора и администратора;
- инфраструктура — Docker Compose и Nginx.

## Что умеет система

| Возможность | Статус | Детали |
|---|---|---|
| Расписание | Реализовано | Администратор задаёт рабочие интервалы врачей через CSV-импорт; слоты генерируются автоматически с учётом перерывов |
| Запись пациента | Реализовано | Пациент выбирает врача и свободный слот; проверяются существование врача и пациента, будущее время, рабочий интервал, перерыв и занятость слота |
| Запись регистратором | Реализовано | Регистратор записывает пациента от его имени |
| Создание пациента | API | `POST /api/v1/patients`; кнопка в веб-клиенте — следующая итерация |
| Идемпотентность записи | Реализовано | Повторный запрос того же пациента на тот же слот возвращает существующую запись |
| Запрет пересечения | Реализовано | У пациента не может быть двух активных записей на одно время; проверка в сервисе и триггер PostgreSQL |
| Перенос и отмена | Реализовано | Отмена принимает причину через API; сбор причины в UI — следующая итерация |
| Журнал операций | Реализовано | `created` / `rescheduled` / `canceled`: кто, когда, что, старая/новая запись и причина (если передана) |
| Расписание врача | Реализовано | День/неделя и список записанных пациентов |
| Пользователи и роли | Реализовано | Администратор меняет роли и специальности, ищет пользователей |
| CSV | Реализовано | Импорт/экспорт врачей, пациентов, записей и рабочих слотов; повторные строки не создают дублей и возвращаются в отчёте импорта |
| Отчёт утилизации | Реализовано | Вкладка «Загрузка расписания»: период, специальность, таблица и экспорт CSV |
| Очистка refresh-токенов | Реализовано | Планировщик внутри API ежедневно удаляет истёкшие токены |

## Быстрый запуск

```bash
git clone https://gitverse.ru/KatacanXD/RegistrationOffice.git
cd RegistrationOffice
cp .env.example .env
docker-compose up -d --build
```

Проверка: открыть `http://localhost/` (SPA) и `http://localhost/swagger-ui/` (OpenAPI).
Для локального backend без Docker: Java 21, PostgreSQL 16, переменные из `.env.example`.

## Документация

Карта документации — в [docs/README.md](docs/README.md).

## Текущие источники истины в коде

- API-контроллеры: `backend/src/main/java/ru/katacan/registrationoffice/controller`
- Security и доступы: `backend/src/main/java/ru/katacan/registrationoffice/config/SecurityConfig.java`
- Модель данных (JPA-сущности): `backend/src/main/java/ru/katacan/registrationoffice/entity`
- DTO и мапперы: `backend/src/main/java/ru/katacan/registrationoffice/dto`, `backend/src/main/java/ru/katacan/registrationoffice/mapper`
- Конфигурация: `backend/src/main/resources/application.properties`
- Схема и триггеры: `backend/src/main/resources/schema-postgresql.sql`
- Контейнеры и прокси: `backend/Dockerfile`, `frontend/Dockerfile`, `frontend/nginx/default.conf`, `deploy/nginx/default.conf`, `docker-compose.yml`

## Что важно помнить

- все публичные JSON-поля идут в `snake_case`;
- refresh token передаётся и обновляется через `HttpOnly` cookie `refresh_token`;
- SPA и API — разные контейнеры: статику отдаёт `frontend`/Nginx, API не доставляет SPA;
- журнал операций — `t_appointment_operations`; отдельная техническая таблица, не часть `t_appointments`;
- причина отмены/переноса передаётся в теле запроса; если не передана, в журнале `null`;
- отчёт утилизации считается по сгенерированным слотам и активным записям, а не хранится отдельно.

## Ограничения текущей версии

- нет внешних интеграций (ЕМИАС, SMS, 1С, оплата);
- нет UI для причины отмены и создания пациента — только API;
- отчёт утилизации без графиков (таблица + CSV);
- тесты backend выполняются на H2; триггеры PostgreSQL проверяются при запуске на реальной БД.

## Правило синхронизации

При изменении API/ролей/модели данных/контейнерного контура обновляются минимум:
1. `README.md` (корень);
2. `docs/05-data-and-integration/*`;
3. `docs/06-final-specification/*`;
4. `docs/07-validation-and-delivery/*`;
5. `docs/90-checkpoints/*`.
