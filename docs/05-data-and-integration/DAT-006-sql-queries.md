# DOC-DAT-006 — Основные SQL-запросы к БД

| Версия | Статус | Дата создания | Дата обновления |
|--------|--------|---------------|-----------------|
| v0.1   | Draft  | 2026-04-10    | 2026-04-12      |

О документе: 6 ключевых SQL-запросов, используемых в системе RegistrationOffice.

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

## Запрос 1: Получить свободные слоты врача на дату

Используется при записи на приём — показать пациенту доступные слоты.

```sql
SELECT generate_series(
    ws.start_time,
    ws.end_time - (ws.slot_minutes || ' minutes')::interval,
    (ws.slot_minutes || ' minutes')::interval
) AS slot_time
FROM work_slots ws
WHERE ws.doctor_id = :doctor_id
  AND ws.date = :date
  AND generate_series NOT BETWEEN ws.break_start AND ws.break_end
EXCEPT
SELECT a.slot_datetime::time
FROM appointments a
WHERE a.doctor_id = :doctor_id
  AND a.slot_datetime::date = :date
  AND a.status = 'booked';
```

---

## Запрос 2: Проверка двойного бронирования

Используется при валидации V5 перед созданием записи.

```sql
SELECT COUNT(*) AS cnt
FROM appointments
WHERE doctor_id = :doctor_id
  AND slot_datetime = :slot_datetime
  AND status = 'booked';
-- Если cnt > 0 → отказ в бронировании
```

---

## Запрос 3: Расписание врача на день (со списком пациентов)

Используется на экране врача — «Моё расписание».

```sql
SELECT a.slot_datetime,
       a.status,
       p.fio AS patient_name,
       p.birth_year
FROM appointments a
JOIN patients p ON a.patient_code = p.patient_code
WHERE a.doctor_id = :doctor_id
  AND a.slot_datetime::date = :date
ORDER BY a.slot_datetime;
```

---

## Запрос 4: Отчёт утилизации по врачу за период

Основной аналитический запрос — отчёт «Загрузка расписания».

```sql
SELECT d.doctor_id,
       d.fio,
       d.specialty,
       ws.date,
       COUNT(DISTINCT slot_time) AS slots_total,
       COUNT(DISTINCT CASE WHEN a.status = 'booked' THEN a.appt_id END) AS slots_booked,
       COUNT(DISTINCT CASE WHEN a.status = 'canceled' THEN a.appt_id END) AS slots_canceled,
       ROUND(
           COUNT(DISTINCT CASE WHEN a.status = 'booked' THEN a.appt_id END)::numeric
           / NULLIF(COUNT(DISTINCT slot_time), 0), 2
       ) AS utilization
FROM doctors d
JOIN work_slots ws ON d.doctor_id = ws.doctor_id
LEFT JOIN appointments a ON a.doctor_id = d.doctor_id
  AND a.slot_datetime::date = ws.date
WHERE ws.date BETWEEN :from_date AND :to_date
GROUP BY d.doctor_id, d.fio, d.specialty, ws.date
ORDER BY ws.date, d.fio;
```

---

## Запрос 5: История записей пациента

Используется на экране «Мои записи» пациента.

```sql
SELECT a.appt_id,
       a.slot_datetime,
       a.status,
       a.cancel_reason,
       d.fio AS doctor_name,
       d.specialty
FROM appointments a
JOIN doctors d ON a.doctor_id = d.doctor_id
WHERE a.patient_code = :patient_code
ORDER BY a.slot_datetime DESC;
```

---

## Запрос 6: Отмена записи с логированием

Используется при отмене — обновление статуса и фиксация причины.

```sql
UPDATE appointments
SET status = 'canceled',
    cancel_reason = :reason,
    updated_at = NOW()
WHERE appt_id = :appt_id
  AND status = 'booked'
RETURNING appt_id, doctor_id, slot_datetime, patient_code;
```
