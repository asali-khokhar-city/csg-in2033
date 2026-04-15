package ac.csg.pu.gui.dashboard.commercial;

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

    private final ProductFeed productFeed = TestDataInitializer.getProductFeed();

    public void setProduct(Product product) {
        this.product = product;
        nameLabel.setText(product.getName());
        merchantLabel.setText(productFeed.getMerchant(product.getMerchantId()).getName());
        originalPriceLabel.setText(String.format("£%.2f", product.getVATPrice()));
        vatLabel.setText(product.isVatExempt() ? "(VAT exempt)" : "(incl. VAT)");
        productImage.setImage(new Image(getClass().getResource("img/placeholder.png").toExternalForm()));
        setupAddButton();
    }

    public void setProduct(Product product, Promotion promotion) {
        this.product = product;
        this.promotion = promotion;

        double basePrice = product.getVATPrice();
        double discount = promotion.getDiscountForProduct(product.getId());
        double discountedPrice = basePrice * (1.0 - (discount / 100.0));

        vatLabel.setText(product.isVatExempt() ? "(VAT exempt)" : "(incl. VAT)");
        originalPriceLabel.setText(String.format("£%.2f", basePrice));

        if (discountedPrice < basePrice) {
            originalPriceLabel.getStyleClass().setAll("old-price");
            discountedPriceLabel.setText(String.format("£%.2f", discountedPrice));
            discountedPriceLabel.setVisible(true);
        }

        nameLabel.setText(product.getName());
        merchantLabel.setText(productFeed.getMerchant(product.getMerchantId()).getName());
        productImage.setImage(new Image(getClass().getResource("img/placeholder.png").toExternalForm()));
        setupAddButton();
    }

    private void setupAddButton() {
        addButton.setOnAction(e -> {
            Cart.incrementProduct(product, promotion);

            if (cartController != null) cartController.refreshCartList();

            // Transfer focus away from the button so the ScrollPane does not auto-scroll to it
            addButton.getParent().requestFocus();

            addButton.getStyleClass().setAll("add-button-success");
            addButton.setText("Added!");
            addButton.setDisable(true);

            PauseTransition pause = new PauseTransition(Duration.millis(900));
            pause.setOnFinished(ev -> {
                addButton.getStyleClass().setAll("add-button");
                addButton.setText("Add to Cart");
                addButton.setDisable(false);
            });
            pause.play();
        });
    }

    public void setCartController(CartController cartController) {
        this.cartController = cartController;
    }
}
