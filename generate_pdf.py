from fpdf import FPDF

pdf = FPDF()
pdf.add_page()
pdf.set_font("Helvetica", size=12)
pdf.cell(200, 10, txt="Hello World!", ln=1, align='C')
pdf.cell(200, 10, txt="This is a test document for SUPER-SYS.", ln=2, align='C')
pdf.output("test_doc.pdf")
