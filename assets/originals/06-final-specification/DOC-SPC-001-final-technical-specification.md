# DOC-SPC-001 Финальная техническая спецификация

| Версия | Статус | Дата обновления |
|---|---|---|
| v1.3 | Draft | 2026-04-18 |

О документе: итоговая техническая спецификация MVP на основе текущих экранов и API.

## Область решения

MVP включает React SPA, Spring Boot API, PostgreSQL и деплой через Docker Compose/Nginx.

## Функциональные модули

- Auth и JWT refresh flow
- Пользователи и роли
- Пациентский кабинет и поиск врача
- Календарь врача и талонов
- Регистраторские сценарии
- CSV import/export для администратора

## Нефункциональные требования

- role-based access на backend и frontend;
- refresh token в `HttpOnly` cookie `refresh_token`;
- JSON в `snake_case`;
- единые contract docs для UI и API;
- воспроизводимый деплой на VM.

## Связанные документы

- API-контракты: `docs/05-data-and-integration/DOC-INT-002-data-exchange-contracts.md`
- Модель данных: `docs/05-data-and-integration/DOC-DAT-001-logical-data-model-and-erd.md`
- Словарь данных: `docs/05-data-and-integration/DOC-DAT-002-data-dictionary-and-validation-rules.md`
- Приемочные сценарии: `docs/07-validation-and-delivery/DOC-TST-001-acceptance-test-scenarios.md`

## Ключевые особенности backend

- access token передаётся в JSON-ответе, refresh token — через `HttpOnly` cookie `refresh_token`;
- JSON-поля сериализуются в `snake_case`;
- public endpoints есть только для просмотра расписания врача;
- админские CSV-операции доступны только роли `admin`.
