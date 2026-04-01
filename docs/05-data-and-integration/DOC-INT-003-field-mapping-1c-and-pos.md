# DOC-INT-003 Маппинг полей для внешних систем

| Версия | Статус | Дата обновления |
|---|---|---|
| v1.0 | Draft | 2026-04-01 |

О документе: резервный документ для будущей интеграции с внешними медицинскими системами.

## Статус MVP

В MVP прямой интеграции с внешними системами нет.

## Черновой маппинг (на будущее)

- `patient.full_name` <-> `t_users.f_fio`
- `patient.policy_number` <-> `t_users.f_policy_number`
- `doctor.speciality` <-> `t_users.f_speciality_name`
- `appointment.datetime` <-> `t_appointments.f_slot_datetime`
- `appointment.status` <-> `t_dict_status.f_status_name`

## Условие активации

Документ актуализируется после утверждения интеграционного backlog и внешнего контрагента.
