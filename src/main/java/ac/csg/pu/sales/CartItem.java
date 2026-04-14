package ac.csg.pu.sales;

import ac.csg.pu.prm.Promotion;
import ac.csg.pu.config.Constants;

public class CartItem {
    private int id;
    private final Product product;
    private final Promotion promotion; // can be null
    private int quantity;

    public CartItem(Product product, Promotion promotion) {
        this.product = product;
        this.promotion = promotion;
        this.quantity = 1;
    }

    public Product getProduct() {
        return product;
    }

    public Promotion getPromotion() {
        return promotion;
    }

    public int getQuantity() {
        return quantity;
    }

    public void incrementQuantity() {
        quantity++;
    }

    public void decrementQuantity() {
        if (quantity > 0) quantity--;
    }

    public double getUnitPrice() {
        double price = product.getPrice();

        // Apply configurable VAT if the product is not medically exempt
        if (!product.isVatExempt()) {
            price = price * (1.0 + Constants.VAT_RATE);
        }

        if (promotion != null) {
            double discount = promotion.getDiscountForProduct(product.getId());
            price = price * (1 - discount / 100.0);
        }
        return price;
    }

    public double getTotalPrice() {
        return getUnitPrice() * quantity;
    }
}