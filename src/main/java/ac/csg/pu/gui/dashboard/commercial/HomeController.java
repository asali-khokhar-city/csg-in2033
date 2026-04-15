package ac.csg.pu.gui.dashboard.commercial;

import ac.csg.pu.gui.SceneHelper;
import ac.csg.pu.gui.util.SessionManager;
import ac.csg.pu.prm.Promotion;
import ac.csg.pu.prm.PromotionDatabase;
import ac.csg.pu.sales.Product;
import ac.csg.pu.sales.ProductFeed;
import ac.csg.pu.test.TestDataInitializer;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.io.IOException;

public class HomeController {

    @FXML private StackPane root;
    @FXML private TextField searchField;
    @FXML private Label errorLabel;
    @FXML private Label cartBadge;
    @FXML private VBox promotionsBox;
    @FXML private TilePane productTilePane;
    @FXML private Rectangle cartOverlay;
    @FXML private AnchorPane cartSidebar;
    @FXML private CartController cartSidebarController;

    private final ProductFeed productFeed = TestDataInitializer.getProductFeed();
    private boolean cartOpen = false;

    @FXML
    public void initialize() {
        setupCartSidebar();
        setupSearch();
        loadProducts();
        loadPromotions();
    }

    private void setupCartSidebar() {
        cartOverlay.widthProperty().bind(root.widthProperty());
        cartOverlay.heightProperty().bind(root.heightProperty());

        cartSidebar.setVisible(false);
        cartSidebar.setManaged(false);
        cartSidebar.setTranslateX(300);

        cartSidebarController.getCloseButton().setOnAction(e -> toggleCart());
        cartSidebarController.getCheckoutButton().setOnAction(e -> SceneHelper.switchScene("dashboard/commercial/checkout.fxml"));

        // Bind the badge count to the cart's item count property
        cartSidebarController.itemCountProperty().addListener((obs, oldVal, newVal) -> {
            int count = newVal.intValue();
            cartBadge.setVisible(count > 0);
            cartBadge.setText(String.valueOf(count));
        });
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldText, query) -> {
            errorLabel.setText("");
            productFeed.filterProducts(p -> p.getName().toLowerCase().contains(query.toLowerCase()));
            refreshProductGrid();
            refreshPromotions(query);
        });
    }

    private void loadProducts() {
        refreshProductGrid();
    }

    private void refreshProductGrid() {
        productTilePane.getChildren().clear();
        for (Product product : productFeed.getFilteredProducts()) {
            productTilePane.getChildren().add(createProductCard(product, null));
        }
    }

    private Node createProductCard(Product product, Promotion promotion) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("product-card.fxml"));
            Node card = loader.load();
            ProductCardController controller = loader.getController();
            controller.setCartController(cartSidebarController);
            if (promotion != null) {
                controller.setProduct(product, promotion);
            } else {
                controller.setProduct(product);
            }
            return card;
        } catch (IOException e) {
            e.printStackTrace();
            return new Label("Error loading product");
        }
    }

    private void loadPromotions() {
        refreshPromotions("");
    }

    private void refreshPromotions(String query) {
        promotionsBox.getChildren().clear();

        for (Promotion promo : PromotionDatabase.getActivePromotions()) {
            TilePane tile = new TilePane();
            tile.setHgap(16);
            tile.setVgap(16);
            tile.getStyleClass().add("promo-tile");

            boolean hasProducts = false;

            for (Product product : productFeed.getFilteredProducts()) {
                Double discount = promo.getDiscounts().get(product.getId());
                if (discount == null) continue;
                if (!query.isEmpty() && !product.getName().toLowerCase().contains(query.toLowerCase())) continue;

                tile.getChildren().add(createProductCard(product, promo));
                hasProducts = true;
            }

            if (!hasProducts) continue;

            Label heading = new Label(promo.getName());
            heading.getStyleClass().add("promo-heading");

            VBox section = new VBox(6, heading, tile);
            section.getStyleClass().add("promo-section");
            promotionsBox.getChildren().add(section);
        }
    }

    @FXML
    private void toggleCart() {
        cartOpen = !cartOpen;

        cartSidebar.setVisible(true);
        cartSidebar.setManaged(true);
        cartOverlay.setVisible(cartOpen);

        TranslateTransition tt = new TranslateTransition(Duration.millis(200), cartSidebar);

        if (cartOpen) {
            tt.setFromX(300);
            tt.setToX(0);
        } else {
            tt.setFromX(0);
            tt.setToX(300);
            tt.setOnFinished(e -> {
                cartSidebar.setVisible(false);
                cartSidebar.setManaged(false);
            });
        }

        tt.play();
    }

    @FXML
    private void checkOrders() {
        SceneHelper.switchScene("dashboard/commercial/orders.fxml");
    }

    @FXML
    private void logout() {
        SessionManager.logout();
        SceneHelper.switchScene("auth/login.fxml");
    }
}
