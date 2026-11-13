"""Patch the filled PZ 3/4 blanks into v2 copies.

Only text replacements are applied; the original files are not modified.
"""

import shutil
from pathlib import Path

from docx import Document


def replace_in_paragraph(paragraph, old, new):
    if old not in paragraph.text:
        return 0
    # Fast path: replacement is fully inside one run.
    for run in paragraph.runs:
        if old in run.text:
            run.text = run.text.replace(old, new)
            return 1
    # Fallback: text spans runs; rebuild the paragraph and keep the first run's formatting.
    font = paragraph.runs[0].font if paragraph.runs else None
    new_text = paragraph.text.replace(old, new)
    paragraph.clear()
    run = paragraph.add_run(new_text)
    if font is not None:
        run.font.name = font.name
        run.font.size = font.size
        run.font.bold = font.bold
    return 1


def replace_everywhere(doc, old, new):
    count = 0
    for paragraph in doc.paragraphs:
        count += replace_in_paragraph(paragraph, old, new)
    for table in doc.tables:
        for row in table.rows:
            for cell in row.cells:
                for paragraph in cell.paragraphs:
                    count += replace_in_paragraph(paragraph, old, new)
    return count


def patch(src, dst, replacements):
    shutil.copyfile(src, dst)
    doc = Document(dst)
    for old, new in replacements:
        changed = replace_everywhere(doc, old, new)
        print(f"{Path(dst).name}: {old!r} -> {changed} change(s)")
    doc.save(dst)


if __name__ == "__main__":
    root = Path(r"C:\Users\Katacan\Documents\RegistrationOffice\3 семак\1 хаккатон")
    patch(
        root / "ПЗ3" / "ПЗ3_Сценарии_Команда1.docx",
        root / "ПЗ3" / "ПЗ3_Сценарии_Команда1_v2.docx",
        [
            ("Команда 1 (самопроверка)", "Команда ___ (проверка соседней командой; протокол — отдельный файл)"),
            ("Управлять пользователями и справочниками", "Управлять пользователями и загружать CSV"),
            ("Вести пользователей, расписание и справочники", "Вести пользователей и загружать данные CSV"),
            ("кто, когда, что и причина", "кто, когда, что; причина — если передана"),
            ("Предлагает создать карточку пациента", "Предлагает создать карточку пациента через API"),
        ],
    )
    patch(
        root / "ПЗ4" / "ПЗ4_Контекст_Команда1.docx",
        root / "ПЗ4" / "ПЗ4_Контекст_Команда1_v2.docx",
        [
            ("Команда 1 (самопроверка)", "Команда ___ (проверка соседней командой; протокол — отдельный файл)"),
            ("Ведёт пользователей и справочники, загружает расписание из файла", "Ведёт пользователей и роли, загружает данные CSV, смотрит отчёт"),
            ("Управляет пользователями и справочниками, импортирует CSV", "Управляет пользователями и ролями, импортирует CSV, смотрит отчёт"),
            ("Управлять пользователями и справочниками", "Управлять пользователями и CSV"),
        ],
    )
