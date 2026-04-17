#!/bin/bash

# Скрипт для тестирования CSV API
# Используйте после запуска приложения и получения JWT токена

TOKEN="your_jwt_token_here"
BASE_URL="http://localhost:8080/api/v1"

echo "===== Тестирование CSV API ====="
echo ""

# Цвета для вывода
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 1. Экспорт врачей
echo -e "${YELLOW}1. Экспорт врачей...${NC}"
curl -X GET "$BASE_URL/admins/export/doctors" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -s | python -m json.tool
echo ""

# 2. Экспорт пациентов
echo -e "${YELLOW}2. Экспорт пациентов...${NC}"
curl -X GET "$BASE_URL/admins/export/patients" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -s | python -m json.tool
echo ""

# 3. Экспорт записей
echo -e "${YELLOW}3. Экспорт записей...${NC}"
curl -X GET "$BASE_URL/admins/export/appointments" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -s | python -m json.tool
echo ""

# 4. Экспорт расписания
echo -e "${YELLOW}4. Экспорт расписания...${NC}"
curl -X GET "$BASE_URL/admins/export/work-slots" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -s | python -m json.tool
echo ""

# 5. Импорт врачей (убедитесь, что файл существует)
if [ -f "examples/doctors.csv" ]; then
  echo -e "${YELLOW}5. Импорт врачей...${NC}"
  curl -X POST "$BASE_URL/admins/import/doctors" \
    -H "Authorization: Bearer $TOKEN" \
    -F "file=@examples/doctors.csv" \
    -s | python -m json.tool
  echo ""
else
  echo -e "${RED}5. Файл examples/doctors.csv не найден${NC}"
  echo ""
fi

# 6. Импорт пациентов
if [ -f "examples/patients.csv" ]; then
  echo -e "${YELLOW}6. Импорт пациентов...${NC}"
  curl -X POST "$BASE_URL/admins/import/patients" \
    -H "Authorization: Bearer $TOKEN" \
    -F "file=@examples/patients.csv" \
    -s | python -m json.tool
  echo ""
else
  echo -e "${RED}6. Файл examples/patients.csv не найден${NC}"
  echo ""
fi

# 7. Импорт записей
if [ -f "examples/appointments.csv" ]; then
  echo -e "${YELLOW}7. Импорт записей...${NC}"
  curl -X POST "$BASE_URL/admins/import/appointments" \
    -H "Authorization: Bearer $TOKEN" \
    -F "file=@examples/appointments.csv" \
    -s | python -m json.tool
  echo ""
else
  echo -e "${RED}7. Файл examples/appointments.csv не найден${NC}"
  echo ""
fi

# 8. Импорт расписания
if [ -f "examples/work_slots.csv" ]; then
  echo -e "${YELLOW}8. Импорт расписания...${NC}"
  curl -X POST "$BASE_URL/admins/import/work-slots" \
    -H "Authorization: Bearer $TOKEN" \
    -F "file=@examples/work_slots.csv" \
    -s | python -m json.tool
  echo ""
else
  echo -e "${RED}8. Файл examples/work_slots.csv не найден${NC}"
  echo ""
fi

echo -e "${GREEN}===== Тестирование завершено =====${NC}"

