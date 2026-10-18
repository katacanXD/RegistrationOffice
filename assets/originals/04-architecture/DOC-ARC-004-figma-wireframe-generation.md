# DOC-ARC-004 — Figma: генерация wireframes и анимации

| Версия | Статус | Дата создания | Дата обновления |
|--------|--------|---------------|-----------------|
| v0.2   | Draft  | 2026-04-02    | 2026-04-18      |

О документе: описание Figma-артефактов для текущих экранов RegistrationOffice — wireframes, прототипы и переходы.

Для кого: designer, frontend-разработчик, заказчик.

Основано
на: [low-fi wireframes](DOC-ARC-003-low-fi-wireframes-and-screen-logic.md), [UX-сценарии](../02-requirements/DOC-REQ-003-ux-scenarios.md).

---

## Авторы документа

- Романчук Даниил — Designer / Tester — создание макетов и анимаций
- Лукьянов Илья — Frontend developer — реализуемость, перенос в код
- Наботов Карим — Project Lead — согласование с требованиями
- Чернышов Ярослав — Backend developer — данные для макетов
- Сысоев Георгий — Backend developer — форматы ответов API

## История изменений

- [v0.1] [2026-04-02] (Романчук, Лукьянов): первичный набор wireframes и анимаций.
- [v0.2] [2026-04-18] (Чернышов): добавлена привязка к API и уточнение данных для макетов.

---

## Ссылка на Figma

> Ссылка на проект:
*[Figma](https://www.figma.com/design/dhSpD9QDRP79l77V0HJsWv/%D0%9E%D0%9F%D0%94?node-id=0-1&p=f&t=QOJygsnFG9sZETfO-0)*

---

## Структура Figma-проекта

```
RegistrationOffice (Figma)
├── 01-Screens/
│   ├── Login
│   ├── Registration
│   ├── Patient-main
│   ├── Patient-search
│   ├── Patient-calendar
│   ├── Doctor-calendar
│   ├── Registrar-patients
│   ├── Registrar-patient
│   ├── Registrar-doctors
│   ├── Registrar-calendar
│   └── Admin-panel
├── 02-Components/
│   ├── Header-with-avatar
│   ├── Back-link
│   ├── Doctor-card
│   ├── Patient-card
│   ├── Slot-button
│   ├── Modal-confirm
│   └── User-row
├── 03-Prototypes/
│   ├── Patient-booking-flow
│   ├── Patient-reschedule-flow
│   ├── Patient-cancel-flow
│   ├── Doctor-view-flow
│   ├── Registrar-booking-flow
│   └── Admin-management-flow
└── 04-Style-guide/
    ├── Colors
    ├── Typography
    ├── Spacing
    └── Icons
```

---

## Анимации (прототипы в Figma)

Каждый прототип соответствует реальному переходу между экранами.

### Анимация 1: Вход и переход по роли

| Шаг | Экран             | Действие                     | Анимация       |
|-----|-------------------|------------------------------|----------------|
| 1   | Login             | Ввод логина и пароля         | Dissolve 200ms |
| 2   | Login             | Нажатие «Войти»              | Smart animate  |
| 3   | Роль пользователя | Переход на стартовый маршрут | Move in 150ms  |

### Анимация 2: Пациент привязывает полис и записывается

| Шаг | Экран            | Действие             | Анимация       |
|-----|------------------|----------------------|----------------|
| 1   | Patient-main     | Ввод полиса ОМС      | Smart animate  |
| 2   | Patient-main     | Нажатие «Сохранить»  | Dissolve 200ms |
| 3   | Patient-search   | Выбор врача          | Smart animate  |
| 4   | Patient-calendar | Выбор слота          | Smart animate  |
| 5   | Modal-confirm    | Подтверждение записи | Dissolve 200ms |
| 6   | Patient-main     | Возврат в кабинет    | Move in 150ms  |

### Анимация 3: Пациент переносит или удаляет запись

| Шаг | Экран            | Действие                                          | Анимация      |
|-----|------------------|---------------------------------------------------|---------------|
| 1   | Patient-main     | Нажатие «Перенести»                               | Smart animate |
| 2   | Patient-calendar | Выбор нового слота                                | Smart animate |
| 3   | Patient-main     | Возврат после успеха                              | Move in 150ms |
| 4   | Patient-main     | Нажатие «Удалить» и подтверждение browser confirm | —             |

### Анимация 4: Врач открывает талон записи

| Шаг | Экран           | Действие              | Анимация       |
|-----|-----------------|-----------------------|----------------|
| 1   | Doctor-calendar | Выбор дня в календаре | Smart animate  |
| 2   | Doctor-calendar | Клик по занятым слоту | Move in 150ms  |
| 3   | Modal-detail    | Просмотр талона       | Dissolve 200ms |

### Анимация 5: Регистратор записывает пациента

| Шаг | Экран              | Действие                    | Анимация       |
|-----|--------------------|-----------------------------|----------------|
| 1   | Registrar-patients | Поиск и выбор пациента      | Smart animate  |
| 2   | Registrar-patient  | Переход к поиску врача      | Move in 150ms  |
| 3   | Registrar-doctors  | Поиск врача                 | Smart animate  |
| 4   | Registrar-calendar | Выбор слота и подтверждение | Dissolve 200ms |

### Анимация 6: Администратор управляет пользователями и импортом

| Шаг | Экран       | Действие                    | Анимация       |
|-----|-------------|-----------------------------|----------------|
| 1   | Admin-panel | Поиск пользователя          | Smart animate  |
| 2   | Admin-panel | Изменение роли / speciality | Dissolve 200ms |
| 3   | Admin-panel | Загрузка CSV-файла          | Move in 150ms  |

---

## Дизайн-система

### Общие принципы

- Минималистичный светлый интерфейс.
- Верхний хедер с логотипом и аватаром пользователя.
- Карточки, модальные окна и кнопки в едином стиле.
- Для пациента и регистратора важны крупные кнопки «Назад», «Записаться», «Перенести», «Удалить».

### Prompt для генерации wireframes

```
Создай low-fi wireframe для веб-системы записи на приём RegistrationOffice.

Экран: [название экрана]
Роль: [patient / doctor / registrar / admin]
Действия пользователя: [список действий]
Данные на экране: [поля, карточки, слоты, модалки]
Стиль: минималистичный веб-интерфейс, светлый фон, верхний хедер, без sidebar.
Язык интерфейса: русский.
```

---

## Связь с документацией

| Экран              | API-эндпоинт                                                                                                                            |
|--------------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| Login              | `POST /api/v1/auth/login`                                                                                                               |
| Registration       | `POST /api/v1/auth/registration`                                                                                                        |
| Patient-main       | `GET /api/v1/patients/me/dashboard`, `PATCH /api/v1/patients/me/policy`                                                                 |
| Patient-search     | `GET /api/v1/doctors`                                                                                                                   |
| Patient-calendar   | `GET /api/v1/doctors/{doctorId}/slots`, `POST /api/v1/appointments/me`, `PATCH /api/v1/appointments/{id}`                               |
| Doctor-calendar    | `GET /api/v1/doctors/{doctorId}/calendar`, `GET /api/v1/doctors/{doctorId}/schedule`, `GET /api/v1/appointments/detail/{appointmentId}` |
| Registrar-patients | `GET /api/v1/patients`                                                                                                                  |
| Registrar-patient  | `GET /api/v1/patients/{patientId}/dashboard`, `DELETE /api/v1/appointments/{id}`                                                        |
| Registrar-doctors  | `GET /api/v1/doctors`                                                                                                                   |
| Registrar-calendar | `GET /api/v1/doctors/{doctorId}/slots`, `POST /api/v1/appointments`, `PATCH /api/v1/appointments/{id}`                                  |
| Admin-panel        | `GET /api/v1/users`, `PATCH /api/v1/users/{userId}`, `POST /api/v1/admins/import/*`                                                     |
