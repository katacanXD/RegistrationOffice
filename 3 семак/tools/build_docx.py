"""Build a DOCX from a small JSON spec.

The built-in dlt_doc_write create path produced empty documents in this workspace,
so this script is the fallback used to generate the practical-work documents.
"""

import argparse
import json
import os

from docx import Document
from docx.enum.section import WD_ORIENT
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Cm, Pt


def set_run_font(run, size=None, bold=None, italic=None):
    run.font.name = "Times New Roman"
    run._element.rPr.rFonts.set(qn("w:eastAsia"), "Times New Roman")
    if size is not None:
        run.font.size = Pt(size)
    if bold is not None:
        run.bold = bold
    if italic is not None:
        run.italic = italic


def add_page_number(paragraph):
    paragraph.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = paragraph.add_run()
    fld_char1 = OxmlElement("w:fldChar")
    fld_char1.set(qn("w:fldCharType"), "begin")
    instr_text = OxmlElement("w:instrText")
    instr_text.set(qn("xml:space"), "preserve")
    instr_text.text = "PAGE"
    fld_char2 = OxmlElement("w:fldChar")
    fld_char2.set(qn("w:fldCharType"), "end")
    run._r.append(fld_char1)
    run._r.append(instr_text)
    run._r.append(fld_char2)
    set_run_font(run, size=10)


def add_paragraph(doc, text, size=12, bold=False, italic=False, align=None, space_after=4):
    p = doc.add_paragraph()
    p.paragraph_format.space_after = Pt(space_after)
    p.paragraph_format.line_spacing = 1.15
    if align == "center":
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    elif align == "justify":
        p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    run = p.add_run(text)
    set_run_font(run, size=size, bold=bold, italic=italic)
    return p


def add_heading(doc, text, level=1):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(10 if level == 1 else 6)
    p.paragraph_format.space_after = Pt(4)
    p.paragraph_format.keep_with_next = True
    run = p.add_run(text)
    set_run_font(run, size=14 if level == 1 else 12, bold=True)
    return p


def add_table(doc, rows, size=10):
    if not rows:
        return
    cols = max(len(row) for row in rows)
    table = doc.add_table(rows=len(rows), cols=cols)
    table.style = "Table Grid"
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = True
    for i, row in enumerate(rows):
        for j in range(cols):
            cell = table.cell(i, j)
            cell.text = ""
            p = cell.paragraphs[0]
            p.paragraph_format.space_after = Pt(0)
            value = row[j] if j < len(row) else ""
            run = p.add_run(str(value))
            set_run_font(run, size=size, bold=(i == 0))
    doc.add_paragraph().paragraph_format.space_after = Pt(2)


def build(spec, out_path):
    doc = Document()

    section = doc.sections[0]
    section.top_margin = Cm(2)
    section.bottom_margin = Cm(2)
    section.left_margin = Cm(3)
    section.right_margin = Cm(1.5)
    if spec.get("landscape"):
        section.orientation = WD_ORIENT.LANDSCAPE
        section.page_width, section.page_height = section.page_height, section.page_width

    font_size = spec.get("font_size", 12)
    normal = doc.styles["Normal"]
    normal.font.name = "Times New Roman"
    normal.font.size = Pt(font_size)
    normal.element.rPr.rFonts.set(qn("w:eastAsia"), "Times New Roman")

    title = spec.get("title")
    if title:
        add_paragraph(doc, title, size=16, bold=True, align="center", space_after=6)
    subtitle = spec.get("subtitle")
    if subtitle:
        add_paragraph(doc, subtitle, size=11, italic=True, align="center", space_after=10)

    for block in spec.get("blocks", []):
        kind = block.get("type")
        if kind == "h1":
            add_heading(doc, block.get("text", ""), level=1)
        elif kind == "h2":
            add_heading(doc, block.get("text", ""), level=2)
        elif kind == "p":
            add_paragraph(
                doc,
                block.get("text", ""),
                size=block.get("size", font_size),
                bold=block.get("bold", False),
                italic=block.get("italic", False),
                align=block.get("align", "justify"),
                space_after=block.get("space_after", 4),
            )
        elif kind == "table":
            add_table(doc, block.get("data", []), size=block.get("size", 10))
        elif kind == "pagebreak":
            doc.add_page_break()
        elif kind == "image":
            path = block.get("path")
            if path and os.path.exists(path):
                doc.add_picture(path, width=Cm(block.get("width_cm", 16)))
                doc.paragraphs[-1].alignment = WD_ALIGN_PARAGRAPH.CENTER
        else:
            raise ValueError(f"Unknown block type: {kind}")

    footer = section.footer.paragraphs[0]
    add_page_number(footer)

    doc.save(out_path)
    print(f"saved: {out_path}")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("spec")
    parser.add_argument("out")
    args = parser.parse_args()
    with open(args.spec, "r", encoding="utf-8") as fh:
        spec = json.load(fh)
    build(spec, args.out)


if __name__ == "__main__":
    main()
