package ac.csg.pu.rpt;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ReportRow {

    private final StringProperty productId;
    private final StringProperty productName;
    private final StringProperty unitPrice;
    private final StringProperty purchasePrice;
    private final StringProperty quantity;
    private final StringProperty total;

    public ReportRow(String productId, String productName,
                     String unitPrice, String purchasePrice,
                     String quantity, String total) {

        this.productId = new SimpleStringProperty(productId);
        this.productName = new SimpleStringProperty(productName);
        this.unitPrice = new SimpleStringProperty(unitPrice);
        this.purchasePrice = new SimpleStringProperty(purchasePrice);
        this.quantity = new SimpleStringProperty(quantity);
        this.total = new SimpleStringProperty(total);
    }

    public StringProperty productIdProperty() { return productId; }
    public StringProperty productNameProperty() { return productName; }
    public StringProperty unitPriceProperty() { return unitPrice; }
    public StringProperty purchasePriceProperty() { return purchasePrice; }
    public StringProperty quantityProperty() { return quantity; }
    public StringProperty totalProperty() { return total; }

    public String getProductId() { return productId.get(); }
    public String getProductName() { return productName.get(); }
    public String getUnitPrice() { return unitPrice.get(); }
    public String getPurchasePrice() { return purchasePrice.get(); }
    public String getQuantity() { return quantity.get(); }
    public String getTotal() { return total.get(); }
}