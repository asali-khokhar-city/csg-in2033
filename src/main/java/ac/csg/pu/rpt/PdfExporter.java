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

/**
 * Exports a list of ReportRows to a PDF file using Apache PDFBox.
 *
 * Renders an A4 page with:
 *   - A bold title at the top
 *   - A date range line
 *   - A separator line
 *   - A header row (Category | Metric | Value | Period)
 *   - All data rows with alternating background shading
 *   - Overflow onto additional pages if there are many rows
 *
 * Usage:
 *   new PdfExporter().export(rows, "Sales Report (UC38)", from, to, outputFile);
 */
public class PdfExporter {

    private static final Logger logger = LoggerFactory.getLogger(PdfExporter.class);

    // Page layout constants (points — 1 point ≈ 0.353 mm)
    private static final float MARGIN_LEFT   = 40f;
    private static final float MARGIN_TOP    = 780f;
    private static final float ROW_HEIGHT    = 18f;
    private static final float HEADER_Y      = MARGIN_TOP - 60f;

    // Column x-positions for Category | Metric | Value | Period
    private static final float[] COL_X = {40f, 180f, 340f, 440f};

    /**
     * Exports the report to a PDF file.
     *
     * @param rows        the report rows to render
     * @param reportTitle the title shown at the top of the PDF
     * @param from        report start date (shown in subtitle)
     * @param to          report end date (shown in subtitle)
     * @param destination the file to write the PDF to
     * @throws IOException if the file cannot be written
     */
    public void export(List<ReportRow> rows, String reportTitle,
                       LocalDate from, LocalDate to, File destination) throws IOException {

        try (PDDocument doc = new PDDocument()) {
            float y = addPage(doc, reportTitle, from, to);
            PDPage currentPage = doc.getPage(doc.getNumberOfPages() - 1);
            // APPEND mode so we don't overwrite the title content already written in addPage
            PDPageContentStream cs = new PDPageContentStream(
                    doc, currentPage, PDPageContentStream.AppendMode.APPEND, true);

            // Draw column headers
            drawHeaderRow(cs, y);
            y -= ROW_HEIGHT;
            drawHorizontalLine(cs, y + 4, MARGIN_LEFT, 560f);
            y -= 4;

            // Draw each data row
            int rowNum = 0;
            for (ReportRow row : rows) {
                // Start a new page if we're running out of space
                if (y < 60f) {
                    cs.close();
                    y = addPage(doc, reportTitle + " (cont.)", from, to);
                    currentPage = doc.getPage(doc.getNumberOfPages() - 1);
                    cs = new PDPageContentStream(
                            doc, currentPage, PDPageContentStream.AppendMode.APPEND, true);
                    drawHeaderRow(cs, y);
                    y -= ROW_HEIGHT;
                    drawHorizontalLine(cs, y + 4, MARGIN_LEFT, 560f);
                    y -= 4;
                    rowNum = 0;
                }

                // Alternating row shading
                if (rowNum % 2 == 0) {
                    drawFilledRect(cs, MARGIN_LEFT, y - 4, 520f, ROW_HEIGHT, 0.95f);
                }

                drawDataRow(cs, y, row);
                y -= ROW_HEIGHT;
                rowNum++;
            }

            // Footer line
            drawHorizontalLine(cs, y + 4, MARGIN_LEFT, 560f);

            cs.close();
            doc.save(destination);
            logger.info("PDF exported to: {}", destination.getAbsolutePath());
        }
    }

    // ── private rendering helpers ─────────────────────────────────────────────

    /**
     * Adds a new A4 page to the document, draws the title and subtitle, and
     * returns the y-coordinate where content should start.
     */
    private float addPage(PDDocument doc, String title, LocalDate from, LocalDate to)
            throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);
        PDPageContentStream cs = new PDPageContentStream(doc, page);

        // Title
        cs.beginText();
        cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
        cs.newLineAtOffset(MARGIN_LEFT, MARGIN_TOP);
        cs.showText("IPOS-PU: " + title);
        cs.endText();

        // Subtitle — date range
        cs.beginText();
        cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
        cs.newLineAtOffset(MARGIN_LEFT, MARGIN_TOP - 20);
        cs.showText("Period: " + formatDate(from) + "  to  " + formatDate(to));
        cs.endText();

        // Separator line under title block
        drawHorizontalLine(cs, MARGIN_TOP - 30, MARGIN_LEFT, 560f);

        cs.close();
        return HEADER_Y;
    }

    private void drawHeaderRow(PDPageContentStream cs, float y) throws IOException {
        cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
        String[] headers = {"Category", "Metric", "Value", "Period"};
        for (int i = 0; i < headers.length; i++) {
            cs.beginText();
            cs.newLineAtOffset(COL_X[i], y);
            cs.showText(headers[i]);
            cs.endText();
        }
    }

    private void drawDataRow(PDPageContentStream cs, float y, ReportRow row) throws IOException {
        cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
        String[] cells = {
                truncate(row.getCategory(), 22),
                truncate(row.getMetric(),   30),
                truncate(row.getValue(),    16),
                truncate(row.getPeriod(),   40)
        };
        for (int i = 0; i < cells.length; i++) {
            cs.beginText();
            cs.newLineAtOffset(COL_X[i], y);
            cs.showText(cells[i] != null ? cells[i] : "");
            cs.endText();
        }
    }

    private void drawHorizontalLine(PDPageContentStream cs, float y,
                                    float xStart, float xEnd) throws IOException {
        cs.setLineWidth(0.5f);
        cs.moveTo(xStart, y);
        cs.lineTo(xEnd, y);
        cs.stroke();
    }

    private void drawFilledRect(PDPageContentStream cs, float x, float y,
                                float width, float height, float grayLevel) throws IOException {
        cs.setNonStrokingColor(grayLevel, grayLevel, grayLevel);
        cs.addRect(x, y, width, height);
        cs.fill();
        cs.setNonStrokingColor(0f, 0f, 0f); // reset to black
    }

    /** Truncates a string to maxLen characters to prevent overflow. */
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

