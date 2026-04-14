package ac.csg.pu.gui.dashboard.commercial;

import ac.csg.pu.config.Constants;
import ac.csg.pu.prm.Promotion;
import ac.csg.pu.sales.Cart;
import ac.csg.pu.sales.Product;
import ac.csg.pu.sales.ProductFeed;
import ac.csg.pu.test.TestDataInitializer;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class ProductCardController {

    @FXML private ImageView productImage;
    @FXML private Label nameLabel;
    @FXML private Label merchantLabel;
    @FXML private Label originalPriceLabel;
    @FXML private Label discountedPriceLabel;
    @FXML private Label vatLabel;
    @FXML private Button addButton;

    private Product product;
    private Promotion promotion;
    private CartController cartController;
    private HomeController homeController;

    private final ProductFeed productFeed = TestDataInitializer.getProductFeed();

    public void setProduct(Product product) {
        this.product = product;

        nameLabel.setText(product.getName());

        String merchantName = productFeed.getMerchant(product.getMerchantId()).getName();
        merchantLabel.setText(merchantName);

        // Apply VAT for display if not exempt
        double displayPrice = product.getVATPrice();
        originalPriceLabel.setText(String.format("£%.2f", displayPrice));
        vatLabel.setText(product.isVatExempt() ? "(VAT exempt)" : "(incl. VAT)");

        productImage.setImage(new Image(
                getClass().getResource("img/placeholder.png").toExternalForm()
        ));

        setupActions();
    }

    public void setProduct(Product product, Promotion promotion) {
        this.product = product;
        this.promotion = promotion;

        double basePrice = product.getVATPrice();
        double discount = promotion.getDiscountForProduct(product.getId());
        double newPrice = basePrice * (1.0 - (discount / 100.0));

        vatLabel.setText(product.isVatExempt() ? "(VAT exempt)" : "(incl. VAT)");
        originalPriceLabel.setText(String.format("£%.2f", basePrice));

        if (basePrice > newPrice) {
            originalPriceLabel.getStyleClass().setAll("old-price");
            discountedPriceLabel.getStyleClass().setAll("new-price");
            discountedPriceLabel.setText(String.format("£%.2f", newPrice));
            discountedPriceLabel.setVisible(true);
        }

        nameLabel.setText(product.getName());

        String merchantName = productFeed.getMerchant(product.getMerchantId()).getName();
        merchantLabel.setText(merchantName);

        productImage.setImage(new Image(
                getClass().getResource("img/placeholder.png").toExternalForm()
        ));

        setupActions();
    }

    private void setupActions() {
        addButton.setOnAction(e -> {
            Cart.incrementProduct(product, promotion);

            if (cartController != null) cartController.refreshCartList();
            if (homeController != null) homeController.updateCartBadge();

            // Transfer focus away from the button so the ScrollPane doesn't auto-scroll to it
            addButton.getParent().requestFocus();

            String originalText = addButton.getText();
            String originalStyle = addButton.getStyle();

            addButton.setText("✓ Added!");
            addButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 6 14; -fx-background-radius: 18; -fx-cursor: hand;");
            addButton.setDisable(true);

            PauseTransition pause = new PauseTransition(Duration.millis(900));
            pause.setOnFinished(ev -> {
                addButton.setText(originalText);
                addButton.setStyle(originalStyle);
                addButton.setDisable(false);
            });
            pause.play();
        });
    }

    public void setCartController(CartController cartController) {
        this.cartController = cartController;
    }

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }
}
