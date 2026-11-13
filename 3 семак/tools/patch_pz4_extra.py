from docx import Document

PATH = r"C:\Users\Katacan\Documents\RegistrationOffice\3 семак\1 хаккатон\ПЗ4\ПЗ4_Контекст_Команда1_v2.docx"

REPLACEMENTS = [
    ("администратор ведёт записи и справочники.",
     "администратор ведёт пользователей, CSV и отчёт."),
    ("расписание, специальности и справочники вводит администратор",
     "расписание и пользователей вводит администратор, данные загружаются CSV"),
    ("Ведёт пользователей и справочники, загружает расписание",
     "Ведёт пользователей и роли, загружает CSV, смотрит отчёт"),
    ("регистратор и администратор ведут записи и справочники.",
     "регистратор ведёт записи, администратор — пользователей, CSV и отчёт."),
    ("администратор — вести справочники.",
     "администратор — вести пользователей, CSV и отчёт."),
    ("смотрит историю визитов.",
     "смотрит свои записи."),
]


def replace_in_paragraph(paragraph, old, new):
    if old not in paragraph.text:
        return 0
    for run in paragraph.runs:
        if old in run.text:
            run.text = run.text.replace(old, new)
            return 1
    font = paragraph.runs[0].font if paragraph.runs else None
    new_text = paragraph.text.replace(old, new)
    paragraph.clear()
    run = paragraph.add_run(new_text)
    if font is not None:
        run.font.name = font.name
        run.font.size = font.size
        run.font.bold = font.bold
    return 1


doc = Document(PATH)
for old, new in REPLACEMENTS:
    count = 0
    for paragraph in doc.paragraphs:
        count += replace_in_paragraph(paragraph, old, new)
    for table in doc.tables:
        for row in table.rows:
            for cell in row.cells:
                for paragraph in cell.paragraphs:
                    count += replace_in_paragraph(paragraph, old, new)
    print(f"{old[:60]!r}: {count}")
doc.save(PATH)
