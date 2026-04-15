package ac.csg.pu.rpt;

import ac.csg.pu.data.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Queries the database to build the Click / Traffic Report (UC36).
 *
 * Produces three groups of rows:
 *   "Traffic Summary" — total clicks, unique users, average clicks per user
 *   "Top Products"    — top 5 products by view count
 *   "Top Events"      — top 5 event types by frequency
 */

public class ClickReportQuery {

    private static final Logger logger = LoggerFactory.getLogger(ClickReportQuery.class);

    public List<ReportRow> query(LocalDate from, LocalDate to) 
    {
        List<ReportRow> rows = new ArrayList<>();
    
        String period = formatPeriod(from, to);

        Timestamp tsFrom = Timestamp.valueOf(from.atStartOfDay());
        Timestamp tsTo   = Timestamp.valueOf(to.atTime(23, 59, 59));

        // ── 1. Traffic summary ─────────────────────────────────────────────────
        String summarySql =
                "SELECT COUNT(*)                  AS total_clicks, " +
                "       COUNT(DISTINCT user_id)   AS unique_users " +
                "FROM clickstream_events " +
                "WHERE timestamp BETWEEN ? AND ?";

        try (Connection conn = Database.connect("orders.db");
           PreparedStatement ps = conn.prepareStatement(summarySql))
              {
            ps.setTimestamp(1, tsFrom);
            ps.setTimestamp(2, tsTo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) 
            {
                int totalClicks  = rs.getInt("total_clicks");
                int uniqueUsers  = rs.getInt("unique_users");
                double avgPerUser = uniqueUsers > 0
                        ? (double) totalClicks / uniqueUsers : 0.0;

                rows.add(new ReportRow("Traffic Summary", "Total Clicks",
                        String.valueOf(totalClicks), period));
                rows.add(new ReportRow("Traffic Summary", "Unique Users",
                        String.valueOf(uniqueUsers), period));
                rows.add(new ReportRow("Traffic Summary", "Avg. Clicks per User",
                        String.format("%.2f", avgPerUser), period));
            }
        }
        ////////////////////////
         catch (SQLException e) {
            logger.error("Click summary query failed", e);
            rows.add(new ReportRow("Error", "Click summary query failed", e.getMessage(), period));
        }
        // ── 2. Top 5 products by view count ────────────────────────────────────

        String productSql ="SELECT target_id, COUNT(*) AS views " +  "FROM clickstream_events " + "WHERE timestamp BETWEEN ? AND ? " + "AND event_type = 'PRODUCT_VIEW' " + "AND target_id IS NOT NULL " + "GROUP BY target_id " + "ORDER BY views DESC " +"LIMIT 5";

        try (Connection conn = Database.connect("orders.db");
             PreparedStatement ps = conn.prepareStatement(productSql)) {
            ps.setTimestamp(1, tsFrom);
            ps.setTimestamp(2, tsTo);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) 
            {
                rows.add(new ReportRow(
                        "Top Products",
                        rs.getString("target_id"),
                        rs.getInt("views") + " views",
                        period));
            }
        } 
        catch (SQLException e) {
            logger.error("Top products query failed", e);
        }

        // ── 3. Top 5 event types by frequency ──────────────────────────────────
        String eventSql =
                "SELECT event_type, COUNT(*) AS freq " +
                "FROM clickstream_events " +
                "WHERE timestamp BETWEEN ? AND ? " +
                "GROUP BY event_type " +
                "ORDER BY freq DESC " +
                "LIMIT 5";

        try (Connection conn = Database.connect("orders.db");
             PreparedStatement ps = conn.prepareStatement(eventSql)) {
            ps.setTimestamp(1, tsFrom);
            ps.setTimestamp(2, tsTo);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                rows.add(new ReportRow(
                        "Top Events",
                        rs.getString("event_type"),
                        String.valueOf(rs.getInt("freq")),
                        period));
            }
        } 
        catch (SQLException e)
        {
            logger.error("Top events query failed", e);
        }
        return rows;
    }
    private String formatPeriod(LocalDate from, LocalDate to) 
    {
        return from.getDayOfMonth() + "/" + from.getMonthValue() + "/" + from.getYear()
                + " - "
                + to.getDayOfMonth() + "/" + to.getMonthValue() + "/" + to.getYear();
    }
}
