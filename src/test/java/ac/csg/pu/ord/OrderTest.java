package ac.csg.pu.ord;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class OrderTest {

    @Test
    void orderFieldsMatchWhatWasPassedIn() {
        Order order = new Order(1, "test@example.com", OrderStatus.ACCEPTED, LocalDate.of(2026, 4, 10), "123 Example Road");
        assertEquals(1, order.getId());
        assertEquals("test@example.com", order.getEmail());
        assertEquals(OrderStatus.ACCEPTED, order.getStatus());
        assertEquals(LocalDate.of(2026, 4, 10), order.getDate());
        assertEquals("123 Example Road", order.getAddress());
    }

    @Test
    void freshOrderHasNoItemsYet() {
        Order order = new Order(2, "test@example.com", OrderStatus.ACCEPTED, LocalDate.of(2026, 4, 15), "123 Example Road");
        assertTrue(order.getItems().isEmpty());
    }

    @Test
    void addOneItemOrderContainsThatItem() {
        Order order = new Order(3, "test@example.com", OrderStatus.ACCEPTED, LocalDate.of(2026, 4, 16), "123 Example Road");
        OrderItem item = new OrderItem(1, "Zinc", 4.00, 4.00, 2);
        order.addItem(item);
        assertEquals(1, order.getItems().size());
    }

    @Test
    void clearingItemsLeavesOrderEmpty() {
        Order order = new Order(4, "test@example.com", OrderStatus.ACCEPTED, LocalDate.of(2026, 4, 17), "123 Example Road");
        order.addItem(new OrderItem(1, "Iron", 3.00, 3.00, 1));
        order.clearItems();
        assertTrue(order.getItems().isEmpty());
    }

    @Test
    void updatingStatusReflectsNewValue() {
        Order order = new Order(5, "test@example.com", OrderStatus.ACCEPTED, LocalDate.of(2026, 4, 18), "123 Example Road");
        order.setStatus(OrderStatus.DELIVERED);
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
    }

    @Test
    void addTwoItemsBothAppearInOrder() {
        Order order = new Order(6, "test@example.com", OrderStatus.SHIPPED, LocalDate.of(2026, 4, 20), "123 Example Road");
        order.addItem(new OrderItem(1, "Vitamin B12", 5.00, 5.00, 1));
        order.addItem(new OrderItem(2, "Omega 3", 8.00, 8.00, 2));
        assertEquals(2, order.getItems().size());
    }
}
