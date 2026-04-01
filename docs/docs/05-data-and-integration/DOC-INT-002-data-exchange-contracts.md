# DOC-INT-002 Контракты обмена данными

| Версия | Статус | Дата обновления |
|---|---|---|
| v1.0 | Draft | 2026-04-01 |

## Правила контрактов

- все endpoint'ы версионируются через `/api/v1`;
- форматы DTO синхронизируются с backend кодом;
- изменения контрактов требуют обновления frontend-клиента и документации.

## Группы endpoint'ов

- `auth`: `/api/v1/auth/*`
- `users`: `/api/v1/users/*`
- `patients`: `/api/v1/patients/*`
- `doctors`: `/api/v1/doctors/*`
- `appointments`: `/api/v1/appointments/*`

## Контрактная дисциплина

- breaking change без мажорного пересмотра версии API не допускается;
- каждое изменение DTO сопровождается changelog-записью в docs.
