# DOC-INT-001 Контуры интеграции и сценарии обмена

| Версия | Статус | Дата обновления |
|---|---|---|
| v1.0 | Draft | 2026-04-01 |

## Контуры

1. Browser -> Nginx -> Frontend (статические ассеты SPA)
2. Browser -> Nginx -> Backend API (`/api/v1/**`)
3. Backend -> PostgreSQL (JPA)

## Основные сценарии обмена

- `login/refresh/logout` для сессии пользователя;
- получение профиля `me`;
- загрузка списка врачей/слотов;
- создание и обновление записи;
- администрирование пользователей.

## CORS и cookie-политика

- CORS настраивается переменными `APP_SECURITY_CORS_ALLOWED_ORIGINS` и `APP_SECURITY_CORS_ALLOWED_ORIGIN_PATTERNS`.
- Для браузерного сценария frontend и backend должны работать под согласованным origin или через единый `nginx` entrypoint.
- Refresh-cookie использует backend-политику (`secure`, `sameSite`, `path`) и должна быть проверена отдельно для dev/prod.

## Нефункциональные требования

- единый JSON-формат ошибок;
- предсказуемые HTTP-коды;
- CORS настраивается через env-переменные.
