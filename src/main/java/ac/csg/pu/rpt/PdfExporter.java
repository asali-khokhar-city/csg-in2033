package ac.csg.pu.rpt;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class PdfExporter {

    private static final Logger logger = LoggerFactory.getLogger(PdfExporter.class);

    private static final float MARGIN_LEFT = 30f;
    private static final float MARGIN_TOP = 780f;
    private static final float ROW_HEIGHT = 18f;
    private static final float HEADER_Y = MARGIN_TOP - 60f;

    // Product ID | Product Name | Unit Price | Purchase Price | Quantity | Total
    private static final float[] COL_X = {30f, 95f, 250f, 330f, 430f, 490f};

    public void export(List<ReportRow> rows, String reportTitle,
                       LocalDate from, LocalDate to, File destination) throws IOException {

        try (PDDocument doc = new PDDocument()) {
            float y = addPage(doc, reportTitle, from, to);
            PDPage currentPage = doc.getPage(doc.getNumberOfPages() - 1);
            PDPageContentStream cs = new PDPageContentStream(
                    doc, currentPage, PDPageContentStream.AppendMode.APPEND, true);

            drawHeaderRow(cs, y, reportTitle);
            y -= ROW_HEIGHT;
            drawHorizontalLine(cs, y + 4, MARGIN_LEFT, 565f);
            y -= 4;
            int rowNum = 0;
            for (ReportRow row : rows) {
                if (y < 60f) {
                    cs.close();
                    y = addPage(doc, reportTitle + " (cont.)", from, to);
                    currentPage = doc.getPage(doc.getNumberOfPages() - 1);
                    cs = new PDPageContentStream(
                            doc, currentPage, PDPageContentStream.AppendMode.APPEND, true);
                    drawHeaderRow(cs, y, reportTitle);
                    y -= ROW_HEIGHT;
                    drawHorizontalLine(cs, y + 4, MARGIN_LEFT, 565f);
                    y -= 4;
                    rowNum = 0;
                }
                if (rowNum % 2 == 0) {
                    drawFilledRect(cs, MARGIN_LEFT, y - 4, 535f, ROW_HEIGHT, 0.95f);
                }
                drawDataRow(cs, y, row, reportTitle);
                y -= ROW_HEIGHT;
                rowNum++;
            }
            drawHorizontalLine(cs, y + 4, MARGIN_LEFT, 565f);
            cs.close();
            doc.save(destination);
            logger.info("PDF exported to: {}", destination.getAbsolutePath());
        }
    }

    private float addPage(PDDocument doc, String title, LocalDate from, LocalDate to)
            throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);
        PDPageContentStream cs = new PDPageContentStream(doc, page);
        cs.beginText();
        cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
        cs.newLineAtOffset(MARGIN_LEFT, MARGIN_TOP);
        cs.showText("IPOS-PU: " + title);
        cs.endText();
        cs.beginText();
        cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
        cs.newLineAtOffset(MARGIN_LEFT, MARGIN_TOP - 20);
        cs.showText("Period: " + formatDate(from) + " to " + formatDate(to));
        cs.endText();
        drawHorizontalLine(cs, MARGIN_TOP - 30, MARGIN_LEFT, 565f);

        cs.close();
        return HEADER_Y;
    }
    private void drawHeaderRow(PDPageContentStream cs, float y, String reportTitle) throws IOException {
        cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 9);
        String[] headers;
        float[] columns;

        if ("Sales Report".equals(reportTitle)) {
            headers = new String[] {
                    "Product ID", "Product Name", "Unit Price",
                    "Purchase Price", "Qty", "Total"
            };
            columns = new float[] {30f, 95f, 250f, 330f, 430f, 490f};
        } else {
            headers = new String[] {
                    "Category", "Metric", "Value", "Period"
            };
            columns = new float[] {30f, 150f, 275f, 400f};
        }
        for (int i = 0; i < headers.length; i++) {
            cs.beginText();
            cs.newLineAtOffset(columns[i], y);
            cs.showText(headers[i]);
            cs.endText();
        }
    }
    private void drawDataRow(PDPageContentStream cs, float y, ReportRow row, String reportTitle) throws IOException {
        cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 8);
        String[] cells;
        float[] columns;
        if ("Sales Report".equals(reportTitle)) {
            cells = new String[] {
                    truncate(row.getProductId(), 10),
                    truncate(row.getProductName(), 24),
                    truncate(row.getUnitPrice(), 12),
                    truncate(row.getPurchasePrice(), 14),
                    truncate(row.getQuantity(), 6),
                    truncate(row.getTotal(), 12)
            };
            columns = new float[] {30f, 95f, 250f, 330f, 430f, 490f};
        } else {
            cells = new String[] {
                    truncate(row.getProductId(), 14),
                    truncate(row.getProductName(), 24),
                    truncate(row.getUnitPrice(), 28),
                    truncate(row.getPurchasePrice(), 28)
            };
            columns =  new float[] {30f, 150f, 275f, 400f};
        }
        for (int i = 0; i < cells.length; i++) {
            cs.beginText();
            cs.newLineAtOffset(columns[i], y);
            cs.showText(cells[i] != null ? cells[i] : "");
            cs.endText();
        }
    }
    private void drawHorizontalLine(PDPageContentStream cs, float y, float xStart, float xEnd) throws IOException {
        cs.setLineWidth(0.5f);
        cs.moveTo(xStart, y);
        cs.lineTo(xEnd, y);
        cs.stroke();
    }
    private void drawFilledRect(PDPageContentStream cs, float x, float y, float width, float height, float grayLevel) throws IOException {
        cs.setNonStrokingColor(grayLevel, grayLevel, grayLevel);
        cs.addRect(x, y, width, height);
        cs.fill();
        cs.setNonStrokingColor(0f, 0f, 0f);
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 1) + "…";
    }

    private String formatDate(LocalDate date) {
        if (date == null) return "";
        return String.format("%02d/%02d/%04d",
                date.getDayOfMonth(), date.getMonthValue(), date.getYear());
    }
}