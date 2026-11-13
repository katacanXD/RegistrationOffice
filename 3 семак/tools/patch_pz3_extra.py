from docx import Document

PATH = r"C:\Users\Katacan\Documents\RegistrationOffice\3 семак\1 хаккатон\ПЗ3\ПЗ3_Сценарии_Команда1_v2.docx"

REPLACEMENTS = [
    ("Предупреждает и предлагает перенести существующую запись",
     "Сообщает, что у пациента уже есть запись на это время, и предлагает выбрать другое"),
    ("Пользователи и роли, справочники, загрузка файла расписания.",
     "Пользователи и роли, CSV-импорт, отчёт утилизации."),
    ("импорт отклоняется целиком с указанием строки",
     "импорт возвращает ошибки по строкам; дубли не создаются"),
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
    print(f"{old[:50]!r}: {count}")
doc.save(PATH)
