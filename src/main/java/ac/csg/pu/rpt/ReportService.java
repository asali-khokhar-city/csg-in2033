package ac.csg.pu.rpt;

import java.time.LocalDate;
import java.util.List;

/**
 * Delegates report generation to the appropriate database query class.
 *
 * Three report types correspond to the use cases:
 *   UC38 — Sales Report       → SalesReportQuery
 *   UC37 — Promotion Report   → PromotionReportQuery
 *   UC36 — Click/Traffic Report → ClickReportQuery
 *
 * All reports honour the supplied date range. The query classes filter data
 * from the database using WHERE timestamp BETWEEN from AND to, so the returned
 * rows only reflect activity within the requested period.
 */
public class ReportService {

    private final SalesReportQuery     salesQuery     = new SalesReportQuery();
    private final PromotionReportQuery promotionQuery = new PromotionReportQuery();
    private final ClickReportQuery     clickQuery     = new ClickReportQuery();

    /**
     * Generates the Sales Report for the given date range (UC38).
     *
     * @param from start date (inclusive)
     * @param to   end date (inclusive)
     * @return list of ReportRow objects ready for display in the TableView
     */
    public List<ReportRow> generateSalesReport(LocalDate from, LocalDate to) {
        return salesQuery.query(from, to);
    }

    /**
     * Generates the Promotion Report for the given date range (UC37).
     *
     * @param from start date (inclusive)
     * @param to   end date (inclusive)
     * @return list of ReportRow objects ready for display in the TableView
     */
    public List<ReportRow> generatePromotionReport(LocalDate from, LocalDate to) {
        return promotionQuery.query(from, to);
    }

    /**
     * Generates the Click / Traffic Report for the given date range (UC36).
     *
     * @param from start date (inclusive)
     * @param to   end date (inclusive)
     * @return list of ReportRow objects ready for display in the TableView
     */
    public List<ReportRow> generateClickReport(LocalDate from, LocalDate to) {
        return clickQuery.query(from, to);
    }
}
