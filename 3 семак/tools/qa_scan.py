import re
from pathlib import Path

from docx import Document

ROOTS = [
    Path(r"C:\Users\Katacan\Documents\RegistrationOffice\3 семак\1 хаккатон"),
    Path(r"C:\Users\Katacan\Documents\RegistrationOffice\3 семак\2 хаккатон"),
    Path(r"C:\Users\Katacan\Documents\RegistrationOffice\3 семак\Защита"),
]

PATTERNS = [
    ("future_date", re.compile(r"2027|2026-12|2026-11-(1[4-9]|[2-9][0-9])")),
    ("five_validations", re.compile(r"5 валидац", re.I)),
    ("self_check", re.compile(r"самопроверк", re.I)),
    ("patient_code", re.compile(r"patient_code")),
    ("old_entities", re.compile(r"\bpatients\b|\bdoctors\b")),
    ("c5_old", re.compile(r"C5 «Сайт")),
    ("c6_old", re.compile(r"C6 «API")),
    ("no_report", re.compile(r"отчёт утилизации отсутствует|экран отчёта.*нет", re.I)),
    ("spravochniki", re.compile(r"справочник", re.I)),
]


def text_of(doc):
    parts = [p.text for p in doc.paragraphs]
    for table in doc.tables:
        for row in table.rows:
            for cell in row.cells:
                parts.append(cell.text)
    return "\n".join(parts)


for root in ROOTS:
    for path in sorted(root.rglob("*.docx")):
        if path.name.startswith("~$"):
            continue
        name = path.name.lower()
        if "v2" not in name and "защита" not in name and "документ" not in name and "протокол" not in name:
            continue
        try:
            doc = Document(path)
        except Exception as exc:
            print(f"SKIP {path}: {exc}")
            continue
        text = text_of(doc)
        hits = []
        for label, pattern in PATTERNS:
            for match in pattern.finditer(text):
                start = max(0, match.start() - 40)
                end = min(len(text), match.end() + 40)
                hits.append((label, text[start:end].replace("\n", " ")))
        if hits:
            print(f"\n{path}")
            for label, snippet in hits[:12]:
                print(f"  [{label}] ...{snippet}...")
