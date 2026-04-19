# DOC-DAT-006 — Основные SQL-запросы к БД

| Версия | Статус | Дата создания | Дата обновления |
|--------|--------|---------------|-----------------|
| v0.2   | Draft  | 2026-04-10    | 2026-04-18      |

О документе: основные SQL-шаблоны для текущей схемы `t_users`, `t_work_slots`, `t_appointments`.

Для кого: backend-разработчики, тестировщик.

Основано на: [ERD](DOC-DAT-001-erd.md), [API](DOC-DAT-003-api-specification.md).

---

## Авторы документа

- Чернышов Ярослав — Backend developer — разработка запросов
- Сысоев Георгий — Backend developer — оптимизация
- Наботов Карим — Project Lead — описание бизнес-логики
- Лукьянов Илья — Frontend developer — требования к данным
- Романчук Даниил — Designer / Tester — проверка результатов

---

## Запрос 1: Получить список врачей с фильтрацией
```text
SELECT u.id AS doctor_id,
       u.full_name AS fio,
       u.speciality
FROM t_users u
JOIN t_dict_role r ON r.f_role_id = u.f_role_id
WHERE r.f_role_name = 'doctor'
  AND (
      :search IS NULL OR :search = ''
      OR LOWER(u.full_name) LIKE LOWER(CONCAT('%', :search, '%'))
      OR LOWER(COALESCE(u.speciality, '')) LIKE LOWER(CONCAT('%', :search, '%'))
  )
ORDER BY u.full_name;
```

## Запрос 2: Получить доступные слоты врача на дату

```text
SELECT ws.f_date,
       gs.slot_time
FROM t_work_slots ws
CROSS JOIN LATERAL generate_series(
    ws.f_start_time,
    ws.f_end_time - (ws.f_slot_minutes || ' minutes')::interval,
    (ws.f_slot_minutes || ' minutes')::interval
) AS gs(slot_time)
WHERE ws.f_doctor_id = :doctor_id
  AND ws.f_date = :date
  AND (ws.f_break_start IS NULL OR gs.slot_time < ws.f_break_start OR gs.slot_time >= ws.f_break_end)
ORDER BY gs.slot_time;
```

## Запрос 3: Проверка двойного бронирования

```text
SELECT COUNT(*) AS cnt
FROM t_appointments
WHERE f_doctor_id = :doctor_id
  AND f_slot_datetime = :slot_datetime
  AND f_status_id = (SELECT f_status_id FROM t_dict_status WHERE f_status_name = 'booked');
```

## Запрос 4: Расписание врача на день

```text
SELECT a.f_appoint_id,
       a.f_slot_datetime,
       s.f_status_name,
       p.f_fio AS patient_fio,
       p.f_policy_number
FROM t_appointments a
JOIN t_dict_status s ON s.f_status_id = a.f_status_id
JOIN t_users p ON p.f_users_id = a.f_patient_id
WHERE a.f_doctor_id = :doctor_id
  AND a.f_slot_datetime::date = :date
ORDER BY a.f_slot_datetime;
```

## Запрос 5: История записей пациента

```text
SELECT a.f_appoint_id,
       a.f_slot_datetime,
       s.f_status_name,
       d.f_fio AS doctor_fio,
       d.f_speciality_name AS speciality
FROM t_appointments a
JOIN t_dict_status s ON s.f_status_id = a.f_status_id
JOIN t_users d ON d.f_users_id = a.f_doctor_id
WHERE a.f_patient_id = :patient_id
ORDER BY a.f_slot_datetime DESC;
```

## Запрос 6: Выборка пользователей для админ-панели

```text
SELECT u.f_users_id,
       u.f_fio,
       r.f_role_name,
       u.f_speciality_name,
       u.f_policy_number
FROM t_users u
JOIN t_dict_role r ON r.f_role_id = u.f_role_id
WHERE (
    :search IS NULL OR :search = ''
    OR LOWER(u.f_fio) LIKE LOWER(CONCAT('%', :search, '%'))
    OR LOWER(COALESCE(u.f_speciality_name, '')) LIKE LOWER(CONCAT('%', :search, '%'))
)
ORDER BY u.f_fio;
```
