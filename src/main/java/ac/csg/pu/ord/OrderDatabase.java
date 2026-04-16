package ac.csg.pu.ord;

import ac.csg.pu.data.DatabaseUtility;
import ac.csg.pu.ord.OrderItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderDatabase {
    private final static Logger logger = LoggerFactory.getLogger(OrderDatabase.class);
    private static final DatabaseUtility db = new DatabaseUtility("orders.db");

    // ---- Table Creation ----
    public static void createTables() {
        String orderTable = "CREATE TABLE IF NOT EXISTS orders ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "customer_email TEXT NOT NULL,"
            + "status TEXT NOT NULL,"
            + "date TEXT,"
            + "address TEXT"
            + ");";
        db.executeUpdate(orderTable);

        String itemTable = "CREATE TABLE IF NOT EXISTS order_items ("
            + "item_id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "order_id INTEGER NOT NULL,"
            + "product_id INTEGER NOT NULL,"
            + "product_name TEXT NOT NULL,"
            + "unit_price REAL NOT NULL,"
            + "purchase_price REAL NOT NULL,"
            + "quantity INTEGER NOT NULL,"
            + "FOREIGN KEY(order_id) REFERENCES orders(id)"
            + ");";
        db.executeUpdate(itemTable);

        logger.info("Order tables created");
    }

    // ---- Order Management ----
    public static int insertOrder(String customerEmail, String status, String date, String address) {
        String sql = "INSERT INTO orders(customer_email, status, date, address) VALUES(?,?,?,?)";
        return db.executeInsert(sql, customerEmail, status, date, address);
    }

    public static boolean orderExists(int orderId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM orders WHERE id = ?)";
        return db.queryBoolean(sql, orderId);
    }

    public static Order getOrder(int orderId) {
        String sql = "SELECT * FROM orders WHERE id=?";

        Order order = db.querySingle(
            sql, rs -> new Order(
                    rs.getInt("id"),
                    rs.getString("customer_email"),
                    OrderStatus.valueOf(rs.getString("status")),
                    LocalDate.parse(rs.getString("date")),
                    rs.getString("address")
            ), orderId
        );

        if (order != null) {
            populateOrderItems(order);
        }

        return order;
    }

    public static List<Order> getOrders(String email) {
        String sql = "SELECT * FROM orders WHERE customer_email=? ORDER BY date DESC";

        List<Order> orders = db.queryMultiple(
            sql, rs -> new Order(
                rs.getInt("id"),
                rs.getString("customer_email"),
                OrderStatus.valueOf(rs.getString("status")),
                LocalDate.parse(rs.getString("date")),
                rs.getString("address")
            ), email
        );

        for (Order order : orders) {
            populateOrderItems(order);
        }

        return orders;
    }

    private static void populateOrderItems(Order order) {
        String sql = "SELECT product_id, product_name, unit_price, purchase_price, quantity FROM order_items WHERE order_id=?";

        List<OrderItem> items = db.queryMultiple(
                sql, rs -> new OrderItem(
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getDouble("unit_price"),
                        rs.getDouble("purchase_price"),
                        rs.getInt("quantity")
                ), order.getId()
        );

        items.forEach(order::addItem);

        for (OrderItem item : items) {
            logger.info("{} [{}] @£{} x{} has been retrieved.", item.productName(), item.productId(), item.purchasePrice(), item.quantity());
        }
    }

    public static void deleteOrder(int orderId) {
        String sql = "DELETE FROM orders WHERE id = ?";
        db.executeUpdate(sql, orderId);
    }

    public static int insertItem(int orderId, int productId, String productName, double unitPrice, double purchasePrice, int quantity) {
        String sql = "INSERT INTO order_items(order_id, product_id, product_name, unit_price, purchase_price, quantity) VALUES(?,?,?,?,?,?)";
        return db.executeInsert(sql, orderId, productId, productName, unitPrice, purchasePrice, quantity);
    }

    public static boolean updateOrderStatus(int orderId, String status) {
        try {
            db.executeUpdate(
                    "UPDATE orders SET status = ? WHERE id = ?",
                    status,
                    orderId
            );
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static Map<Integer, OrderItem> getItemMap(int orderId) {
        String sql = "SELECT item_id, product_id, product_name, unit_price, purchase_price, quantity FROM order_items WHERE order_id=?";

        Map<Integer, OrderItem> itemMap = new HashMap<>();

        db.queryMultiple(sql, rs -> {
            itemMap.put(rs.getInt("item_id"), new OrderItem(
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getDouble("unit_price"),
                    rs.getDouble("purchase_price"),
                    rs.getInt("quantity")
            ));
            return null;
        }, orderId);

        return itemMap;
    }
}