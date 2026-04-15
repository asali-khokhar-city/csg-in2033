package ac.csg.pu.rpt;

import ac.csg.pu.data.DatabaseUtility;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportService {
    private static final DatabaseUtility ordersDb = new DatabaseUtility("orders.db");
    private static final DatabaseUtility promotionsDb = new DatabaseUtility("promotions.db");
    public List<ReportRow> generateReport(String type, LocalDate from, LocalDate to) {
        if (type == null || from == null || to == null) {
            return new ArrayList<>();
        }
        return switch (type) {
            case "Sales Report" -> generateSalesReport(from, to);
            case "Campaign Report" -> generateCampaignReport(from, to);
            case "Customer Engagement Report" -> generateCustomerEngagementReport(from, to);
            default -> new ArrayList<>();
        };
    }
    private List<ReportRow> generateSalesReport(LocalDate from, LocalDate to) {
        List<ReportRow> rows = new ArrayList<>();
        String sql = """
            SELECT
                oi.product_id,
                oi.product_name,
                oi.unit_price,
                oi.purchase_price,
                SUM(oi.quantity) AS total_quantity,
                SUM(oi.quantity * oi.purchase_price) AS total_price
            FROM orders o
            JOIN order_items oi ON o.id = oi.order_id
            WHERE date(o.date) BETWEEN date(?) AND date(?)
            GROUP BY
                oi.product_id,
                oi.product_name,
                oi.unit_price,
                oi.purchase_price
            ORDER BY oi.product_id, oi.purchase_price
            """;
        List<ReportRow> results = ordersDb.queryMultiple(
                sql,
                rs -> new ReportRow(
                        String.valueOf(rs.getInt("product_id")),
                        rs.getString("product_name"),
                        String.format("£%.2f", rs.getDouble("unit_price")),
                        String.format("£%.2f", rs.getDouble("purchase_price")),
                        String.valueOf(rs.getInt("total_quantity")),
                        String.format("£%.2f", rs.getDouble("total_price"))
                ),
                from.toString(),
                to.toString()
        );
        if (results.isEmpty()) {
            rows.add(new ReportRow("", "No sales found", "", "", "", formatPeriod(from, to)));
            return rows;
        }
        rows.addAll(results);
        return rows;
    }
    private List<ReportRow> generateCampaignReport(LocalDate from, LocalDate to) {
        List<ReportRow> rows = new ArrayList<>();
        String sql = """
        SELECT p.name, p.start_date, p.end_date, d.discount_percent
        FROM promotions p
        LEFT JOIN promotion_discounts d ON p.id = d.promotion_id
        WHERE date(p.start_date) <= date(?)
          AND date(p.end_date) >= date(?)
        ORDER BY p.start_date
        """;
        promotionsDb.queryMultiple(sql, rs -> {
            rows.add(new ReportRow(
                    "Campaign",
                    rs.getString("name"),
                    String.format("%.2f%%", rs.getDouble("discount_percent")),
                    rs.getString("start_date") + " - " + rs.getString("end_date"),
                    "",
                    ""
            ));
            return null;
        }, to.toString(), from.toString());
        if (rows.isEmpty()) {
            rows.add(new ReportRow("", "No campaigns found", "", formatPeriod(from, to), "", ""));
        }
        return rows;
    }
    private List<ReportRow> generateCustomerEngagementReport(LocalDate from, LocalDate to) {
        List<ReportRow> rows = new ArrayList<>();

        String sql = """
        SELECT p.name, d.discount_percent, d.hits, d.purchases
        FROM promotions p
        JOIN promotion_discounts d ON p.id = d.promotion_id
        WHERE p.start_date <= ? AND p.end_date >= ?
        ORDER BY p.name, d.discount_percent
        """;

        promotionsDb.queryMultiple(sql, rs -> {
            int hits = rs.getInt("hits");
            int purchases = rs.getInt("purchases");

            String conversion = hits == 0
                    ? "0.00%"
                    : String.format("%.2f%%", (purchases * 100.0) / hits);

            rows.add(new ReportRow(
                    "Campaign",
                    rs.getString("name") + " (" + rs.getDouble("discount_percent") + "%)",
                    "Hits: " + hits + " | Purchases: " + purchases,
                    "Conversion: " + conversion,
                    "",
                    ""
            ));
            return null;
        }, to.toString(), from.toString());

        if (rows.isEmpty()) {
            rows.add(new ReportRow(
                    "",
                    "No engagement data found for selected period",
                    "",
                    formatPeriod(from, to),
                    "",
                    ""
            ));
        }

        return rows;
    }
    private String formatPeriod(LocalDate from, LocalDate to) {
        return from.getDayOfMonth() + "/" + from.getMonthValue() + "/" + from.getYear()
                + " - "
                + to.getDayOfMonth() + "/" + to.getMonthValue() + "/" + to.getYear();
    }
}