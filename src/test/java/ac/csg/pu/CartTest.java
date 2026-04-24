package ac.csg.pu;

import ac.csg.pu.sales.Cart;
import ac.csg.pu.sales.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CartTest {

    private Product product;

    @BeforeEach
    void setup() {
        Cart.clear();
        product = new Product(1, "Ibuprofen", 4.00, 1, true);
    }

    @Test
    void addOneItemCartHasOneEntry() {
        Cart.incrementProduct(product, null);
        assertEquals(1, Cart.getItems().size());
    }

    @Test
    void addSameItemTwiceQuantityIsTwo() {
        Cart.incrementProduct(product, null);
        Cart.incrementProduct(product, null);
        assertEquals(2, Cart.getItems().values().iterator().next().getQuantity());
    }

    @Test
    void removeOneOfTwoItemsQuantityDropsToOne() {
        Cart.incrementProduct(product, null);
        Cart.incrementProduct(product, null);
        Cart.decrementProduct(product, null);
        assertEquals(1, Cart.getItems().values().iterator().next().getQuantity());
    }

    @Test
    void removeOnlyItemCartBecomesEmpty() {
        Cart.incrementProduct(product, null);
        Cart.decrementProduct(product, null);
        assertTrue(Cart.getItems().isEmpty());
    }

    @Test
    void clearCartAfterAddingItemLeavesNothing() {
        Cart.incrementProduct(product, null);
        Cart.clear();
        assertTrue(Cart.getItems().isEmpty());
    }

    @Test
    void twoItemsTotalPriceIsDouble() {
        Cart.incrementProduct(product, null);
        Cart.incrementProduct(product, null);
        assertEquals(8.00, Cart.getTotalPrice(), 0.001);
    }

    @Test
    void nothingInCartTotalIsZero() {
        assertEquals(0.0, Cart.getTotalPrice(), 0.001);
    }
}
