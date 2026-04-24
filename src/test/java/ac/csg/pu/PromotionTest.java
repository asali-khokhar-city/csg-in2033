package ac.csg.pu;

import ac.csg.pu.prm.Promotion;
import ac.csg.pu.sales.CartItem;
import ac.csg.pu.sales.Product;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class PromotionTest {

    @Test
    void currentPromoWithinDateRangeShowsAsActive() {
        Promotion promo = new Promotion(1, "April Sale", true,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30),
                new HashMap<>());
        assertTrue(promo.isCurrentlyActive());
    }

    @Test
    void promoEndedBeforeTodayIsNotActive() {
        Promotion promo = new Promotion(2, "March Sale", true,
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31),
                new HashMap<>());
        assertFalse(promo.isCurrentlyActive());
    }

    @Test
    void promoDisabledByAdminIsNotActive() {
        Promotion promo = new Promotion(3, "Paused Deal", false,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30),
                new HashMap<>());
        assertFalse(promo.isCurrentlyActive());
    }

    @Test
    void promoStartingNextMonthIsNotYetActive() {
        Promotion promo = new Promotion(4, "May Sale", true,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                new HashMap<>());
        assertFalse(promo.isCurrentlyActive());
    }

    @Test
    void tenPercentOffCorrectlyReducesItemPrice() {
        HashMap<Integer, Double> discounts = new HashMap<>();
        discounts.put(1, 10.0);
        Promotion promo = new Promotion(5, "10% Off Omega 3", true, null, null, discounts);
        Product product = new Product(1, "Omega 3", 10.00, 1, true);
        CartItem item = new CartItem(product, promo);
        assertEquals(9.00, item.getPurchasePrice(), 0.001);
    }

    @Test
    void fiftyPercentOffHalvesPurchasePrice() {
        HashMap<Integer, Double> discounts = new HashMap<>();
        discounts.put(1, 50.0);
        Promotion promo = new Promotion(6, "Half Price Biotin", true, null, null, discounts);
        Product product = new Product(1, "Biotin", 20.00, 1, true);
        CartItem item = new CartItem(product, promo);
        assertEquals(10.00, item.getPurchasePrice(), 0.001);
    }

    @Test
    void productNotInPromoGetsNoDiscount() {
        Promotion promo = new Promotion(7, "Selective Discount", true, null, null, new HashMap<>());
        Product product = new Product(99, "Ibuprofen", 5.00, 1, true);
        CartItem item = new CartItem(product, promo);
        assertEquals(5.00, item.getPurchasePrice(), 0.001);
    }

    @Test
    void noPromoAttachedMeansFullPrice() {
        Product product = new Product(1, "Vitamin D", 8.00, 1, true);
        CartItem item = new CartItem(product, null);
        assertEquals(8.00, item.getPurchasePrice(), 0.001);
    }

    @Test
    void promoFieldsMatchWhatWasPassedIn() {
        LocalDate start = LocalDate.of(2026, 4, 1);
        LocalDate end = LocalDate.of(2026, 4, 30);
        Promotion promo = new Promotion(8, "April Deals", true, start, end, new HashMap<>());
        assertEquals(8, promo.getId());
        assertEquals("April Deals", promo.getName());
        assertTrue(promo.isActive());
        assertEquals(start, promo.getStartDate());
        assertEquals(end, promo.getEndDate());
    }
}
