package ac.csg.pu;

import ac.csg.pu.sales.Product;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ProductTest {

    @Test
    void vatExemptMedicineKeepsBasePrice() {
        Product p = new Product(1, "Penicillin", 5.00, 1, true);
        assertEquals(5.00, p.getVATPrice(), 0.001);
    }

    @Test
    void nonExemptVitaminGets20PercentAdded() {
        Product p = new Product(2, "Vitamin C", 10.00, 1, false);
        assertEquals(12.00, p.getVATPrice(), 0.001);
    }

    @Test
    void productFieldsMatchWhatWasPassedIn() {
        Product p = new Product(3, "Zinc", 6.00, 2, false);
        assertEquals(3, p.getId());
        assertEquals("Zinc", p.getName());
        assertEquals(6.00, p.getPrice(), 0.001);
        assertEquals(2, p.getMerchantId());
        assertFalse(p.isVatExempt());
    }

    @Test
    void vatExemptFlagStoredCorrectly() {
        Product p = new Product(4, "Iron", 4.00, 1, true);
        assertTrue(p.isVatExempt());
    }
}
