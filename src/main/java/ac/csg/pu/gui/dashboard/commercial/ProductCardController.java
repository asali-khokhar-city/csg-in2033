package ac.csg.pu.gui.dashboard.commercial;

import ac.csg.pu.prm.Promotion;
import ac.csg.pu.sales.Cart;
import ac.csg.pu.sales.Product;
import ac.csg.pu.sales.ProductFeed;
import ac.csg.pu.test.TestDataInitializer;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ProductCardController {

    @FXML private ImageView productImage;
    @FXML private Label nameLabel;
    @FXML private Label merchantLabel;
    @FXML private Label originalPriceLabel;
    @FXML private Label discountedPriceLabel;
    @FXML private Button addButton;

    private Product product;
    private Promotion promotion;
    private CartController cartController;

    private final ProductFeed productFeed = TestDataInitializer.getProductFeed();

    public void setProduct(Product product) {
        this.product = product;

        nameLabel.setText(product.getName());

        // Merchant lookup
        String merchantName = productFeed.getMerchant(product.getMerchantId()).getName();
        merchantLabel.setText(merchantName);

        originalPriceLabel.setText("£" + product.getPrice());

        // Placeholder image
        productImage.setImage(new Image(
                getClass().getResource("img/placeholder.png").toExternalForm()
        ));

        setupActions();
    }

    public void setProduct(Product product, Promotion promotion) {
        this.product = product;
        this.promotion = promotion;

        double price = product.getPrice();
        double discount = promotion.getDiscountForProduct(product.getId());
        double newPrice = price * (1 - discount / 100.0);

        originalPriceLabel.setText(String.format("£%.2f", price));

        if (price > newPrice) {
            originalPriceLabel.getStyleClass().setAll("old-price");
            discountedPriceLabel.getStyleClass().setAll("new-price");
            discountedPriceLabel.setText(String.format("£%.2f", newPrice));
            discountedPriceLabel.setVisible(true);
        }

        nameLabel.setText(product.getName());

        // Merchant lookup
        String merchantName = productFeed.getMerchant(product.getMerchantId()).getName();
        merchantLabel.setText(merchantName);

        // Placeholder image
        productImage.setImage(new Image(
                getClass().getResource("img/placeholder.png").toExternalForm()
        ));

        setupActions();
    }

    private void setupActions() {
        addButton.setOnAction(e -> {
            Cart.incrementProduct(product, promotion);

            if (cartController != null) {
                cartController.refreshCartList();
            }
        });
    }

    public void setCartController(CartController cartController) { this.cartController = cartController; }
}