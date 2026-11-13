import json
from pathlib import Path

src = Path(r"C:\Users\Katacan\Documents\RegistrationOffice\3 семак\tools\spec_model.json")
dst = Path(r"C:\Users\Katacan\Documents\RegistrationOffice\3 семак\tools\spec_pz7_blank.json")
spec = json.loads(src.read_text(encoding="utf-8"))
spec["title"] = "Практическое занятие №7. Модель данных — Команда 1 (v2)"
spec["subtitle"] = "Группа ЭФБО-10-25. Версия 2 от 07.11.2026. Бланки 1–7; документ «Модель данных» — отдельный файл."

rename = {
    "1. Хранилище": "Вводные данные: хранилище",
    "2. Кандидаты в сущности": "Бланк 1. Кандидаты в сущности",
    "3. Сущности": "Бланк 2. Сущности",
    "4. Связи": "Бланк 3. Связи",
    "5. Правила данных": "Бланк 4. Правила данных",
    "6. ERD и что не вошло": "Бланк 5. ERD-диаграмма",
    "7. Сверка": "Бланк 6. Сверка",
    "8. Проверка соседней командой": "Бланк 7. Проверка соседней командой",
    "9. Физическая реализация": "Примечание: физическая реализация",
}
for block in spec["blocks"]:
    if block.get("type") == "h1" and block.get("text") in rename:
        block["text"] = rename[block["text"]]

dst.write_text(json.dumps(spec, ensure_ascii=False, indent=2), encoding="utf-8")
print("saved", dst)
