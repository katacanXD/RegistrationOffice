from docx import Document

PATHS = [
    r"C:\Users\Katacan\Documents\RegistrationOffice\3 семак\1 хаккатон\ПЗ3\ПЗ3_Сценарии_Команда1_v2.docx",
    r"C:\Users\Katacan\Documents\RegistrationOffice\3 семак\1 хаккатон\ПЗ4\ПЗ4_Контекст_Команда1_v2.docx",
]


def replace(paragraph, old, new):
    if old not in paragraph.text:
        return 0
    for run in paragraph.runs:
        if old in run.text:
            run.text = run.text.replace(old, new)
            return 1
    paragraph.text = paragraph.text.replace(old, new)
    return 1


for path in PATHS:
    doc = Document(path)
    count = 0
    for p in doc.paragraphs:
        count += replace(p, "31.10.2026", "07.11.2026")
    for table in doc.tables:
        for row in table.rows:
            for cell in row.cells:
                for p in cell.paragraphs:
                    count += replace(p, "31.10.2026", "07.11.2026")
    doc.save(path)
    print(path, count)
