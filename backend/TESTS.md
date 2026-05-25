# Тесты

Как запустить тесты и собрать отчёт покрытия:

1. Запуск всех тестов:

```bash
./gradlew test
```

2. Сгенерировать отчёт JaCoCo (HTML + XML):

```bash
./gradlew jacocoTestReport
```

Отчёт в HTML будет доступен в `build/reports/jacoco/test/html/index.html`.

Примеры тестов добавлены в `src/test/java`:
- Unit-тесты сервисов и контроллеров (Mockito + MockMvc)
- Репозиторные тесты (@DataJpaTest, H2 in-memory)
- Тесты обработчиков исключений
