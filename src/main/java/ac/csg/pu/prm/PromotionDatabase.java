package ac.csg.pu.prm;

import ac.csg.pu.data.DatabaseUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ac.csg.pu.data.DatabaseUtility.*;

public class PromotionDatabase {
    private final static Logger logger = LoggerFactory.getLogger(PromotionDatabase.class);
    private static final DatabaseUtility db = new DatabaseUtility("promotions.db");

    // ---- Table creation ----
    public static void createTables() {
        String promoTable = "CREATE TABLE IF NOT EXISTS promotions ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "name TEXT NOT NULL,"
            + "active INTEGER DEFAULT 1,"
            + "start_date TEXT,"
            + "end_date TEXT,"
            + "discount_percent REAL DEFAULT 0"
            + ");";
        db.executeUpdate(promoTable);

        String discountTable = "CREATE TABLE IF NOT EXISTS promotion_discounts ("
            + "promotion_id INTEGER NOT NULL,"
            + "product_id INTEGER NOT NULL,"
            + "discount_percent REAL NOT NULL,"
            + "PRIMARY KEY(promotion_id, product_id),"
            + "FOREIGN KEY(promotion_id) REFERENCES promotions(id)"
            + ");";
        db.executeUpdate(discountTable);
        db.executeUpdate("ALTER TABLE promotion_discounts ADD COLUMN hits INTEGER DEFAULT 0");
        db.executeUpdate("ALTER TABLE promotion_discounts ADD COLUMN purchases INTEGER DEFAULT 0");
        logger.info("Promotion tables created");
    }

    // ---- Promotion management ----
    public static int insertPromotion(String name, boolean active, String startDate, String endDate, double discountPercent) {
        String sql = "INSERT INTO promotions(name, active, start_date, end_date, discount_percent) VALUES(?, ?, ?, ?, ?)";
        return db.executeInsert(sql, name, active ? 1 : 0, startDate, endDate, discountPercent);
    }
    public static int insertPromotion(String name, boolean active, String startDate, String endDate) {
        return insertPromotion(name, active, startDate, endDate, 0);
    }

    public static void deletePromotion(int promotionId) {
        String sql = "DELETE FROM promotions WHERE id = ?";
        db.executeUpdate(sql, promotionId);
    }

    public static void insertDiscount(int promotionId, int productId, double discountPercent) {
        String sql = "INSERT OR REPLACE INTO promotion_discounts(promotion_id, product_id, discount_percent) VALUES(?,?,?)";
        db.executeUpdate(sql, promotionId, productId, discountPercent);
        logger.info("Inserted discount: promoId={}, productId={}, discount={}", promotionId, productId, discountPercent);
    }

    public static double getDiscount(int promotionId, int productId) {
        String sql = "SELECT discount_percent FROM promotion_discounts WHERE promotion_id=? AND product_id=?";
        return db.queryDouble(sql, promotionId, productId);
    }

    public static Map<Integer, Double> getDiscountMap(int promotionId) {
        String sql = "SELECT product_id, discount_percent " +
            "FROM promotion_discounts " +
            "WHERE promotion_id = ?";

        Map<Integer, Double> discountMap = new HashMap<>();

        db.queryMultiple(
            sql, rs -> {
                discountMap.put(rs.getInt("product_id"), rs.getDouble("discount_percent"));
                return null;
            }, promotionId
        );

        return discountMap;
    }

    public static boolean isPromotionActive(int promotionId) {
        String sql = "SELECT active FROM promotions WHERE id=?";
        return db.queryInt(sql, promotionId) == 1;
    }

    public static List<Promotion> getActivePromotions() {
        String sql = "SELECT id, name, start_date, end_date, active FROM promotions WHERE active = 1 AND date('now') BETWEEN start_date AND end_date";

        // Step 1 — collect promotions without nested queries
        List<Promotion> promotions = db.queryMultiple(sql, rs -> {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            LocalDate start = LocalDate.parse(rs.getString("start_date"));
            LocalDate end = LocalDate.parse(rs.getString("end_date"));
            boolean active = rs.getInt("active") == 1;
            return new Promotion(id, name, active, start, end, new HashMap<>());
        });

        // Step 2 — fetch discounts separately after first connection is closed
        for (Promotion p : promotions) {
            Map<Integer, Double> discountMap = getDiscountMap(p.getId());
            logger.info("Promotion {} has {} discounts: {}", p.getName(), discountMap.size(), discountMap);
            discountMap.forEach(p::addProductDiscount);
        }

        return promotions;
    }

    public static List<Promotion> getAllPromotions() {
        String sql = "SELECT id, name, start_date, end_date, active FROM promotions";

        // Step 1 — collect promotions without nested queries
        List<Promotion> promotions = db.queryMultiple(sql, rs -> {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            LocalDate start = LocalDate.parse(rs.getString("start_date"));
            LocalDate end = LocalDate.parse(rs.getString("end_date"));
            boolean active = rs.getInt("active") == 1;
            return new Promotion(id, name, active, start, end, new HashMap<>());
        });

        // Step 2 — fetch discounts separately after first connection is closed
        for (Promotion p : promotions) {
            Map<Integer, Double> discountMap = getDiscountMap(p.getId());
            logger.info("Promotion {} has {} discounts: {}", p.getName(), discountMap.size(), discountMap);
            discountMap.forEach(p::addProductDiscount);
        }

        return promotions;
    }

    public static void setPromotionActive(int promotionId, boolean active) {
        String sql = "UPDATE promotions SET active=? WHERE id=?";
        db.executeUpdate(sql, active ? 1 : 0, promotionId);
    }

    public static void updatePromotion(int id, String name, String startDate, String endDate) {
        String sql = "UPDATE promotions SET name=?, start_date=?, end_date=? WHERE id=?";
        db.executeUpdate(sql, name, startDate, endDate, id);
    }

    public static String getPromotionName(int promotionId) {
        String sql = "SELECT name FROM promotions WHERE id=?";
        return db.queryString(sql, promotionId);
    }

    public static String getStartDate(int promotionId) {
        String sql = "SELECT start_date FROM promotions WHERE id=?";
        return db.queryString(sql, promotionId);
    }

    public static String getEndDate(int promotionId) {
        String sql = "SELECT end_date FROM promotions WHERE id=?";
        return db.queryString(sql, promotionId);
    }
}