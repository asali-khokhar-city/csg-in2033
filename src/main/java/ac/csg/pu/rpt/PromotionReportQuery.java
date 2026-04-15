package ac.csg.pu.rpt;

import ac.csg.pu.data.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
/**
 * Queries the database to build the Promotion Report (UC37).
 *
 * Produces three groups of rows:
 *   "Campaign Summary" — totals: discounts, promotions run, ROI
 *   "Conversion"       — click → cart add → purchase funnel with conversion rate
 *   "Top Campaigns"    — top 3 campaigns by discount granted
 */
public class PromotionReportQuery {

    private static final Logger logger = LoggerFactory.getLogger(PromotionReportQuery.class);

    public List<ReportRow> query(LocalDate from, LocalDate to) {
        List<ReportRow> rows = new ArrayList<>();
        String period = formatPeriod(from, to);

        Timestamp tsFrom = Timestamp.valueOf(from.atStartOfDay());
        Timestamp tsTo   = Timestamp.valueOf(to.atTime(23, 59, 59));

        // ── 1. Campaign summary totals ─────────────────────────────────────────
        String summarySql =
                "SELECT " +
                "  COALESCE(SUM(discount_amt), 0)                                         AS total_disc, " +
                "  COUNT(DISTINCT campaign_id)                                             AS campaign_count, " +
                "  SUM(CASE WHEN event_type = 'CLICK'    THEN 1 ELSE 0 END)               AS clicks, " +
                "  SUM(CASE WHEN event_type = 'CART_ADD' THEN 1 ELSE 0 END)               AS cart_adds, " +
                "  SUM(CASE WHEN event_type = 'PURCHASE' THEN 1 ELSE 0 END)               AS purchases, " +
                "  SUM(CASE WHEN event_type = 'PURCHASE' THEN discount_amt ELSE 0 END)    AS purchase_disc " +
                "FROM promotion_events " +
                "WHERE timestamp BETWEEN ? AND ?";

        try (Connection conn = Database.connect("promotions.db");
             PreparedStatement ps = conn.prepareStatement(summarySql)) {
            ps.setTimestamp(1, tsFrom);
            ps.setTimestamp(2, tsTo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double totalDisc    = rs.getDouble("total_disc");
                int    campaigns    = rs.getInt("campaign_count");
                int    clicks       = rs.getInt("clicks");
                int    cartAdds     = rs.getInt("cart_adds");
                int    purchases    = rs.getInt("purchases");
                double purchaseDisc = rs.getDouble("purchase_disc");

                // Conversion rate = purchases / cart adds × 100
                double convRate = cartAdds > 0 ? (purchases * 100.0 / cartAdds) : 0.0;

                // ROI = revenue generated from promotions / cost of discounts
                // Here we approximate: if discount generates a purchase, revenue = order value
                // We only have discount amount, so ROI = (purchases / clicks) × 100 as engagement ROI
                double roiPct = clicks > 0 ? (purchases * 100.0 / clicks) : 0.0;

                rows.add(new ReportRow("Campaign Summary", "Total Discounts Granted",
                        String.format("£%.2f", totalDisc), period));
                rows.add(new ReportRow("Campaign Summary", "Total Promotions Run",
                        String.valueOf(campaigns), period));
                rows.add(new ReportRow("Campaign Summary", "Engagement ROI",
                        String.format("%.1f%%", roiPct), period));

                rows.add(new ReportRow("Conversion", "Total Clicks",
                        String.valueOf(clicks), period));
                rows.add(new ReportRow("Conversion", "Items Added to Cart",
                        String.valueOf(cartAdds), period));
                rows.add(new ReportRow("Conversion", "Purchases Completed",
                        String.valueOf(purchases), period));
                rows.add(new ReportRow("Conversion", "Conversion Rate",
                        String.format("%.1f%%", convRate), period));
            }
        } catch (SQLException e) {
            logger.error("Promotion summary query failed", e);
            rows.add(new ReportRow("Error", "Promotion summary query failed", e.getMessage(), period));
        }

        // ── 2. Top 3 campaigns by discount granted ─────────────────────────────
        String topSql =
                "SELECT campaign_name, SUM(discount_amt) AS camp_disc " +
                "FROM promotion_events " +
                "WHERE timestamp BETWEEN ? AND ? AND event_type = 'PURCHASE' " +
                "GROUP BY campaign_id, campaign_name " +
                "ORDER BY camp_disc DESC " +
                "LIMIT 3";

        try (Connection conn = Database.connect("promotions.db");
             PreparedStatement ps = conn.prepareStatement(topSql)) {
            ps.setTimestamp(1, tsFrom);
            ps.setTimestamp(2, tsTo);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                rows.add(new ReportRow(
                        "Top Campaigns",
                        rs.getString("campaign_name"),
                        String.format("£%.2f", rs.getDouble("camp_disc")),
                        period));
            }
        } catch (SQLException e) {
            logger.error("Top campaigns query failed", e);
        }

        return rows;
    }

    private String formatPeriod(LocalDate from, LocalDate to) {
        return from.getDayOfMonth() + "/" + from.getMonthValue() + "/" + from.getYear()
                + " - "
                + to.getDayOfMonth() + "/" + to.getMonthValue() + "/" + to.getYear();
    }
}
