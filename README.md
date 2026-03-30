# RegistrationOffice Backend API

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk" alt="Java 21">
  <img src="https://img.shields.io/badge/Spring_Boot-4.0.3-brightgreen?style=for-the-badge&logo=spring-boot" alt="Spring Boot 4">
  <img src="https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql" alt="PostgreSQL">
  <img src="https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker" alt="Docker">
</p>

Backend API для системы медицинской регистратуры: управление пользователями, врачами, пациентами и графиком приёмов.

---

## Что реализовано

* **Auth & Security** — регистрация (BCrypt), JWT-аутентификация, ролевая модель (admin, registrar, doctor, patient)
* **Management** — CRUD пользователей, поиск с пагинацией по пациентам и врачам
* **Scheduling** — рабочие слоты врача, календарь и расписание
* **Appointments** — создание, отмена и редактирование записей на приём
* **DataInitializer** — автоматическая генерация тестовых данных при первом запуске

## Технологии

* Java 21 + Spring Boot 4.0.3 (Web, Data JPA, Security, Validation)
* PostgreSQL 16
* JWT (io.jsonwebtoken / jjwt)
* Docker + Docker Compose + Nginx
* Lombok
* Gradle 9.3.1

---

## Исправленные ошибки компиляции

При сборке проекта возникали **12 ошибок компиляции**. Ниже — что было не так и как исправлено:

### 1. Рассогласование сигнатур в `UserService` / `UserServiceImpl` / контроллерах

**Проблема:** интерфейс `UserService` объявлял методы поиска без параметра `Pageable`:

```java
DoctorListDto searchDoctors(String search);
PatientListDto searchPatients(String search);
UserListDto searchUsers(String search);
```

Но реализация в `UserServiceImpl` и вызовы в контроллерах (`UserController`, `PatientController`, `DoctorController`) использовали `Pageable` и дополнительный параметр `specialty`.

**Исправление:** сигнатуры в интерфейсе `UserService` приведены в соответствие с реализацией:

```java
DoctorListDto searchDoctors(String search, String specialty, Pageable pageable);
PatientListDto searchPatients(String search, Pageable pageable);
UserListDto searchUsers(String search, Pageable pageable);
```

### 2. Приватные методы `getAllUsers/getAllDoctors/getAllPatients` в `UserServiceImpl`

**Проблема:** эти методы вызывали `userRepository.findAllDoctors()` / `findAllPatients()` **без аргументов**, хотя репозиторий требует `Pageable`. Аналогично вызывали `userMapper.toUserList(dtos)` / `toDoctorList(dtos)` / `toPatientList(dtos)` **с одним аргументом**, хотя маппер требует два (`List` + `Page`).

**Исправление:** удалены неиспользуемые приватные методы. Вся функциональность покрыта методами `searchUsers`, `searchPatients`, `searchDoctors` с пагинацией.

### 3. Дублированные импорты в `UserRepository`

**Проблема:** импорты `User`, `JpaRepository`, `Query`, `Param`, `Repository` были продублированы дважды.

**Исправление:** дубликаты удалены.

### 4. Невалидная перегрузка `findByFullName` в `UserRepository`

**Проблема:** Spring Data JPA не может сгенерировать реализацию для `findByFullName(String fullName, String password)` — у сущности `User` нет такого derived-query маппинга по двум полям `fullName` + `password`.

**Исправление:** удалён невалидный метод, оставлен `findByFullName(String fullName)`.

### 5. Лишние поля пагинации в `PatientListDto.PatientDto`

**Проблема:** вложенный DTO `PatientDto` содержал поля `totalElements`, `totalPages`, `currentPage` — это данные пагинации, которые относятся к внешнему `PatientListDto`, а не к каждому элементу списка.

**Исправление:** поля пагинации удалены из `PatientDto`.

---

## Безопасность (роли)

| Эндпоинт | Доступ |
| :--- | :--- |
| `POST /api/v1/registration` | Публичный |
| `POST /api/v1/login` | Публичный |
| `/api/v1/admins/**` | admin |
| `/api/v1/users/**` | admin, registrar |
| `/api/v1/patients/**` | patient (свои данные), admin/registrar (список) |
| `/api/v1/doctors/**` | Все авторизованные |
| Остальные | Требуют аутентификацию |

---

## Запуск локально (Docker Compose)

### Требования

* Docker 24+ и Docker Compose v2
* (Опционально) JDK 21 + Gradle — для локальной разработки без Docker

### Шаги

```bash
# 1. Клонировать репозиторий
git clone https://gitverse.ru/KatacanXD/RegistrationOffice.git
cd RegistrationOffice

# 2. Создать .env из примера
cp .env.example .env

# 3. Отредактировать .env — задать свои значения
nano .env

# 4. Запустить всё в Docker
docker compose up -d --build

# 5. Проверить статус
docker compose ps
docker compose logs -f backend
```

### Переменные окружения (.env)

| Переменная | Описание | Пример |
| :--- | :--- | :--- |
| `DB_NAME` | Имя базы данных PostgreSQL | `registration_office` |
| `DB_USER` | Пользователь PostgreSQL | `registration_user` |
| `DB_PASS` | Пароль PostgreSQL | `strongpassword123` |
| `SECURITY_JWT_SECRET` | Секрет для подписи JWT (мин. 32 символа) | `my_super_secret_key_change_me_please` |
| `APP_SECURITY_CORS_ALLOWED_ORIGINS` | Разрешённые CORS-источники | `https://app.example.com` |
| `APP_SECURITY_CORS_ALLOWED_ORIGIN_PATTERNS` | Паттерны CORS-источников | `https://*.example.com` |

### Архитектура контейнеров

```
nginx:80 -> backend:8080 -> postgres:5432
```

* **nginx** — reverse proxy, слушает порт 80
* **backend** — Spring Boot приложение
* **postgres** — база данных с persistent volume

---

## Деплой на сервер (CI/CD)

Проект использует GitLab CI-совместимый пайплайн (`.gitlab-ci.yml`), который работает на GitVerse.

### Как работает пайплайн

1. **build** — компиляция Java-кода (проверка что проект собирается). Запускается на `master` и merge requests.
2. **deploy** — деплой на сервер по SSH. Запускается **вручную** (кнопка в CI) только для ветки `master`:
   - Синхронизирует файлы проекта на сервер через `rsync`
   - Перезапускает Docker Compose на сервере

### Настройка CI/CD

#### 1. Где менять данные сервера для деплоя

В файле `.gitlab-ci.yml` в секции `variables`:

```yaml
variables:
  DEPLOY_HOST: "178.154.216.17"    # <-- IP вашего сервера
  DEPLOY_USER: "nabotov"           # <-- SSH-пользователь на сервере
  DEPLOY_PATH: "/home/nabotov/RegistrationOffice"  # <-- путь на сервере
```

#### 2. SSH-ключ

В настройках CI/CD репозитория на GitVerse добавьте переменную:

* **Имя:** `SSH_PRIVATE_KEY`
* **Значение:** приватный SSH-ключ (содержимое `id_rsa`), у которого публичная часть добавлена в `~/.ssh/authorized_keys` на сервере

#### 3. Подготовка сервера (первый раз)

```bash
# Установить Docker
sudo apt-get update
sudo apt-get install -y docker.io docker-compose-plugin

# Создать директорию проекта
mkdir -p /home/nabotov/RegistrationOffice

# Создать .env
cd /home/nabotov/RegistrationOffice
cp .env.example .env
nano .env  # задать реальные значения
```

#### 4. Полезные команды на сервере

```bash
# Просмотр логов
sudo docker compose logs -f backend

# Перезапуск
sudo docker compose down && sudo docker compose up -d --build

# Статус контейнеров
sudo docker compose ps
```

### Доступ к API после деплоя

После деплоя API доступно по адресу сервера:

* `http://<DEPLOY_HOST>/api/v1/login`
* `http://<DEPLOY_HOST>/api/v1/registration`
* `http://<DEPLOY_HOST>/api/v1/doctors`

---

## Запуск без Docker (разработка)

```bash
# Нужен работающий PostgreSQL и JDK 21

# Настроить подключение к БД
# Отредактировать src/main/resources/application.properties

# Запустить
./gradlew bootRun
```
