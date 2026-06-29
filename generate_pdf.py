from fpdf import FPDF

pdf = FPDF()
pdf.add_page()
pdf.set_font("Arial", size=12)
pdf.cell(200, 10, txt="Hello World!", ln=1, align='C')
pdf.cell(200, 10, txt="This is a test document for SUPER-SYS AI Deep Analysis.", ln=2, align='C')
pdf.cell(200, 10, txt="Important Decision: Use Spring Boot for microservices.", ln=3, align='C')
pdf.cell(200, 10, txt="Risk: High latency if LLM is slow.", ln=4, align='C')
pdf.output("test_doc.pdf")
