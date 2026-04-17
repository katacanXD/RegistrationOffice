# DOC-SPC-001 Финальная техническая специ��икация

| Версия | Статус | Дата обновления |
|---|---|---|
| v1.2 | Draft | 2026-04-17 |

О документе: финальная техническая спецификация MVP без дублирования API-контрактов и тест-кейсов.

## Область решения

MVP включает fullstack-контур: frontend SPA + backend API + PostgreSQL + Docker Compose deployment.

## Функциональные модули

- Auth и безопасность
- Пользователи и роли
- Врачи и расписание
- Записи на прием
- Администрирование CSV

## Нефункциональные требования

- role-based access;
- refresh token в HttpOnly cookie;
- предсказуемые HTTP-коды и ошибки;
- воспроизводимый деплой на VM;
- обновляемая документация по контрактам.

## Связанные документы

- API-контракты: `docs/05-data-and-integration/DOC-INT-002-data-exchange-contracts.md`
- Модель данных: `docs/05-data-and-integration/DOC-DAT-001-logical-data-model-and-erd.md`
- Словарь данных: `docs/05-data-and-integration/DOC-DAT-002-data-dictionary-and-validation-rules.md`
- Приемочные сценарии: `docs/07-validation-and-delivery/DOC-TST-001-acceptance-test-scenarios.md`

## Ключевые особенности backend

- access token передаётся в JSON-ответе, refresh token — через `HttpOnly` cookie `refresh_token`;
- JSON-поля сериализуются в `snake_case`;
- public endpoints для расписания врача доступны без авторизации.
