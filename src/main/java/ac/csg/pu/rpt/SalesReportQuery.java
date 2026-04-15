package ac.csg.pu.rpt;

import ac.csg.pu.data.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Queries the database to build the Sales Report (UC38).
 *
 * Produces three groups of rows:
 *   "Sales"       — headline totals: revenue, net, VAT, transaction count
 *   "By Product"  — top 5 products by revenue within the date range
 *   "By User"     — top 5 users by spend (proxy for merchant breakdown)
 *
 * All figures are filtered to the requested date range via the timestamp column.
 */
public class SalesReportQuery {

    private static final Logger logger = LoggerFactory.getLogger(SalesReportQuery.class);

    public List<ReportRow> query(LocalDate from, LocalDate to) {
        List<ReportRow> rows = new ArrayList<>();
        String period = formatPeriod(from, to);

        Timestamp tsFrom = Timestamp.valueOf(from.atStartOfDay());
        Timestamp tsTo   = Timestamp.valueOf(to.atTime(23, 59, 59));

        // ── 1. Headline totals ────────────────────────────────────────────────
        String totalsSql =
                "SELECT COALESCE(SUM(total), 0)    AS revenue, " +
                "       COALESCE(SUM(discount), 0) AS total_disc, " +
                "       COALESCE(SUM(vat), 0)      AS vat_total, " +
                "       COUNT(*)                    AS txn_count " +
                "FROM sales_transactions " +
                "WHERE timestamp BETWEEN ? AND ?";

        try (Connection conn = Database.connect("orders.db");
             PreparedStatement ps = conn.prepareStatement(totalsSql)) {
            ps.setTimestamp(1, tsFrom);
            ps.setTimestamp(2, tsTo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double revenue  = rs.getDouble("revenue");
                double disc     = rs.getDouble("total_disc");
                double vat      = rs.getDouble("vat_total");
                int    count    = rs.getInt("txn_count");

                rows.add(new ReportRow("Sales", "Total Revenue",           fmt(revenue),          period));
                rows.add(new ReportRow("Sales", "Net (after discounts)",   fmt(revenue - disc),   period));
                rows.add(new ReportRow("Sales", "Total Discounts Given",   fmt(disc),             period));
                rows.add(new ReportRow("Sales", "VAT Collected (20%)",     fmt(vat),              period));
                rows.add(new ReportRow("Sales", "Total Transactions",      String.valueOf(count), period));
            }
        } catch (SQLException e) {
            logger.error("Sales totals query failed", e);
            rows.add(new ReportRow("Error", "Sales totals query failed", e.getMessage(), period));
        }

        // ── 2. Top 5 products by revenue ──────────────────────────────────────
        String productSql =
                "SELECT i.product_name, SUM(i.line_total) AS product_rev " +
                "FROM sales_transaction_items i " +
                "JOIN sales_transactions t ON i.txn_id = t.txn_id " +
                "WHERE t.timestamp BETWEEN ? AND ? " +
                "GROUP BY i.product_id, i.product_name " +
                "ORDER BY product_rev DESC " +
                "LIMIT 5";

        try (Connection conn = Database.connect("orders.db");
             PreparedStatement ps = conn.prepareStatement(productSql)) {
            ps.setTimestamp(1, tsFrom);
            ps.setTimestamp(2, tsTo);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                rows.add(new ReportRow(
                        "By Product",
                        rs.getString("product_name"),
                        fmt(rs.getDouble("product_rev")),
                        period));
            }
        } catch (SQLException e) {
            logger.error("Product breakdown query failed", e);
        }

        // ── 3. Top 5 users by spend ───────────────────────────────────────────
        String userSql =
                "SELECT user_id, SUM(total) AS user_total, COUNT(*) AS orders " +
                "FROM sales_transactions " +
                "WHERE timestamp BETWEEN ? AND ? AND user_id IS NOT NULL " +
                "GROUP BY user_id " +
                "ORDER BY user_total DESC " +
                "LIMIT 5";

        try (Connection conn = Database.connect("orders.db");
             PreparedStatement ps = conn.prepareStatement(userSql)) {
            ps.setTimestamp(1, tsFrom);
            ps.setTimestamp(2, tsTo);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                rows.add(new ReportRow(
                        "By User",
                        rs.getString("user_id") + " (" + rs.getInt("orders") + " orders)",
                        fmt(rs.getDouble("user_total")),
                        period));
            }
        } catch (SQLException e) {
            logger.error("User breakdown query failed", e);
        }

        return rows;
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private String fmt(double value) {
        return String.format("£%.2f", value);
    }

    private String formatPeriod(LocalDate from, LocalDate to) {
        return from.getDayOfMonth() + "/" + from.getMonthValue() + "/" + from.getYear()
                + " - "
                + to.getDayOfMonth() + "/" + to.getMonthValue() + "/" + to.getYear();
    }
}
