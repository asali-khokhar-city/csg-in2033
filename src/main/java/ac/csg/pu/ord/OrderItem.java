package ac.csg.pu.ord;

public record OrderItem(int productId, String productName, double unitPrice, double purchasePrice, int quantity) {
    public double getTotalPrice() {
        return purchasePrice * quantity;
    }
}