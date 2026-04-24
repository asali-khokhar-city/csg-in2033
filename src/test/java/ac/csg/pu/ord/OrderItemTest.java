package ac.csg.pu.ord;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OrderItemTest {

    @Test
    void totalPriceIsQuantityMultipliedByPurchasePrice() {
        OrderItem item = new OrderItem(1, "Ibuprofen", 6.00, 5.40, 3);
        assertEquals(16.20, item.totalPrice(), 0.001);
    }

    @Test
    void singleQuantityTotalMatchesPurchasePrice() {
        OrderItem item = new OrderItem(2, "Vitamin C", 4.00, 4.00, 1);
        assertEquals(4.00, item.totalPrice(), 0.001);
    }

    @Test
    void allFieldsReturnCorrectValues() {
        OrderItem item = new OrderItem(3, "Biotin", 7.00, 6.30, 2);
        assertEquals(3, item.productId());
        assertEquals("Biotin", item.productName());
        assertEquals(7.00, item.unitPrice(), 0.001);
        assertEquals(6.30, item.purchasePrice(), 0.001);
        assertEquals(2, item.quantity());
    }

    @Test
    void discountedItemHasLowerPurchasePriceThanUnit() {
        OrderItem item = new OrderItem(4, "Omega 3", 10.00, 9.00, 1);
        assertNotEquals(item.unitPrice(), item.purchasePrice());
    }
}
