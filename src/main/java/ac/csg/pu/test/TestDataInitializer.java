package ac.csg.pu.test;

import ac.csg.pu.members.UserType;
import ac.csg.pu.members.UserDatabase;
import ac.csg.pu.prm.PromotionDatabase;
import ac.csg.pu.sales.Merchant;
import ac.csg.pu.sales.Product;
import ac.csg.pu.sales.ProductFeed;

import java.time.LocalDate;
import java.util.Map;

public class TestDataInitializer {

    private static final ProductFeed productFeed = new ProductFeed();

    public static void init() {
        initUsers();
        initMerchants();
        initProducts();
        initPromos();
    }

    public static void initMerchants() {
        productFeed.addMerchant(new Merchant(1, "PharmaCorp"));
        productFeed.addMerchant(new Merchant(2, "HealthCo"));
    }

    public static void initProducts() {
        productFeed.addProduct(new Product(1, "Aspirin", 5.0, 1, true));
        productFeed.addProduct(new Product(2, "Analgin", 7.5, 1, false));
        productFeed.addProduct(new Product(3, "Celebrex, caps 100 mg", 20.0, 2, false));
        productFeed.addProduct(new Product(4, "Retin-A Tretin, 30 g", 15.0, 2, false));
    }

    // Call this method to populate test users
    public static void initUsers() {
        // Example test data
        UserDatabase.createTable(); // make sure table exists
        UserDatabase.insertAdmin("admin@example.com", "password123");
        UserDatabase.insertUser("user1@example.com", "password123", UserType.C.name());
        UserDatabase.insertUser("user2@example.com", "password123", UserType.C.name());
    }

    public static void initPromos() {
        // Example promotions
        addPromotionIfNotExists("Summer Sale", Map.of(
                1, 5.0,
                2, 10.0,
                3, 10.0,
                4, 20.0
        ), LocalDate.now().minusDays(1), LocalDate.now().plusDays(30));

        addPromotionIfNotExists("Winter Clearance", Map.of(
                1, 15.0,
                2, 20.0,
                3, 25.0,
                4, 30.0
        ), LocalDate.now().minusDays(10), LocalDate.now().plusDays(20));

        addPromotionIfNotExists("Flash Discount", Map.of(
                1, 50.0,
                2, 40.0
        ), LocalDate.now(), LocalDate.now().plusDays(1));
    }

    private static void addPromotionIfNotExists (String name, Map < Integer, Double > discounts,
                                                 LocalDate startDate, LocalDate endDate){
        // Check existing promotions by name
        boolean exists = PromotionDatabase.getActivePromotions()
                .stream()
                .anyMatch(p -> p.getName().equals(name));

        if (!exists) {
            int promoId = PromotionDatabase.insertPromotion(
                    name,
                    true,
                    startDate.toString(),
                    endDate.toString()
            );

            // Insert discounts for each product
            discounts.forEach((productId, percent) ->
                    PromotionDatabase.insertDiscount(promoId, productId, percent)
            );
        }
    }

    public static ProductFeed getProductFeed() { return productFeed; }
}