# 🏥 RegistrationOffice Backend API

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk" alt="Java 21">
  <img src="https://img.shields.io/badge/Spring_Boot-4.0.3-brightgreen?style=for-the-badge&logo=spring-boot" alt="Spring Boot 4">
  <img src="https://img.shields.io/badge/PostgreSQL-14+-blue?style=for-the-badge&logo=postgresql" alt="PostgreSQL">
  <img src="https://img.shields.io/badge/Security-Spring_Security-red?style=for-the-badge&logo=spring-security" alt="Spring Security">
</p>

Backend API для системы медицинской регистратуры: комплексное управление пользователями, врачами, пациентами и графиком приемов.

---

## ✨ Что уже реализовано

* 🔐 Auth & Security: Регистрация с хэшированием пароля (BCrypt), логин и ролевая модель доступа.
* 👥 Management: Полный CRUD по пользователям, поиск пациентов и врачей.
* 📅 Scheduling: Логика рабочих слотов врача, календарь и расписание.
* 📑 Appointments: Создание, отмена и редактирование записей на прием.
* 🛠 Автоматизация: Компонент DataInitializer для генерации тестовой среды при первом запуске.

## 🛠 Технологии

* Язык: Java 21
* Фреймворк: Spring Boot 4.0.3 (Web, Data JPA, Security)
* БД: PostgreSQL
* Утилиты: Lombok, JJWT (io.jsonwebtoken)

---

## 🔐 Текущее состояние безопасности

| Эндпоинт | Доступ |
| :--- | :--- |
| POST /api/v1/registration | Публичный |
| POST /api/v1/login | Публичный |
| /api/v1/admins/** | ROLE_ADMIN |
| /api/v1/users/** | ROLE_ADMIN, ROLE_REGISTRAR |
| Все остальные | Требуют аутентификацию |

> Note: На данный момент при логине возвращается dummy-token. Полноценное внедрение JWT находится в процессе.

---

## 🚀 Запуск проекта

### Требования
* JDK 21
* PostgreSQL 14+
* Gradle (используется wrapper)

### Конфигурация
Отредактируйте src/main/resources/application.properties:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=admin
spring.jpa.hibernate.ddl-auto=update