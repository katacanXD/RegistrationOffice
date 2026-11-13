import json
from pathlib import Path

src = Path(r"C:\Users\Katacan\Documents\RegistrationOffice\3 семак\tools\spec_components.json")
dst = Path(r"C:\Users\Katacan\Documents\RegistrationOffice\3 семак\tools\spec_pz6_blank.json")
spec = json.loads(src.read_text(encoding="utf-8"))
spec["title"] = "Практическое занятие №6. Компоненты — Команда 1 (v2)"
spec["subtitle"] = "Группа ЭФБО-10-25. Версия 2 от 07.11.2026. Бланки 1–7; документ «Компоненты контейнера» — отдельный файл."

rename = {
    "1. Выбранный контейнер и его связи": "Бланк 1. Выбранный контейнер и его связи",
    "2. Компоненты": "Бланк 2. Компоненты",
    "3. Связи": "Бланк 3. Связи",
    "4. Требования к качеству и типовые изменения": "Бланк 4. Требования к качеству и типовые изменения",
    "5. Диаграмма компонентов": "Бланк 5. Диаграмма компонентов",
    "6. Что не показано и почему": "Бланк 5 (продолжение). Что не показано",
    "7. Сверка": "Бланк 6. Сверка",
    "8. Проверка соседней командой": "Бланк 7. Проверка соседней командой",
    "9. Ограничения": "Примечание",
}
for block in spec["blocks"]:
    if block.get("type") == "h1" and block.get("text") in rename:
        block["text"] = rename[block["text"]]

dst.write_text(json.dumps(spec, ensure_ascii=False, indent=2), encoding="utf-8")
print("saved", dst)
