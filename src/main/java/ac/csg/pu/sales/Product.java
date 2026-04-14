package ac.csg.pu.sales;

public class Product {
    private final int id;
    private final String name;
    private final double price;
    private final int merchantId;
    private final boolean isVatExempt;

    public Product(int id, String name, double price, int merchantId, boolean isVatExempt) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.merchantId = merchantId;
        this.isVatExempt = isVatExempt;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getMerchantId() { return merchantId; }
    public boolean isVatExempt() {
        return isVatExempt;
    }
}