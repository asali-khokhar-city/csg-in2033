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
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.io.IOException;

public class HomeController {

    @FXML private StackPane root;

    @FXML private TextField searchField;
    @FXML private Label errorLabel;
    @FXML private Accordion promotionAccordion;
    @FXML private TilePane productTilePane;

    @FXML private Rectangle cartOverlay;
    @FXML private AnchorPane cartSidebar;
    @FXML private CartController cartSidebarController;

    private final ProductFeed productFeed = TestDataInitializer.getProductFeed();

    private boolean cartOpen = false;

    // --- INIT ---
    @FXML
    public void initialize() {
        setupCartSidebar();
        setupSearch();

        loadProducts();
        loadPromotions();
    }

    // --- SEARCH ---
    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldText, query) -> {
            errorLabel.setText("");

            productFeed.filterProducts(product ->
                    product.getName().toLowerCase().contains(query.toLowerCase())
            );

            refreshProductGrid();
            refreshPromotions(query);
        });
    }

    // --- PRODUCTS ---
    private void loadProducts() {
        refreshProductGrid();
    }

    private void refreshProductGrid() {
        productTilePane.getChildren().clear();

        for (Product product : productFeed.getFilteredProducts()) {
            Node card = createProductCard(product, null);
            productTilePane.getChildren().add(card);
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

    // --- PROMOTIONS ---
    private void loadPromotions() {
        promotionAccordion.getPanes().clear();

        for (Promotion promo : PromotionDatabase.getActivePromotions()) {
            TitledPane pane = createPromotionPane(promo, "");
            pane.getStyleClass().add("promo-pane");
            promotionAccordion.getPanes().add(pane);
        }
    }

    private void refreshPromotions(String query) {
        promotionAccordion.getPanes().clear();

        for (Promotion promo : PromotionDatabase.getActivePromotions()) {
            TitledPane pane = createPromotionPane(promo, query);
            if (pane != null) {
                promotionAccordion.getPanes().add(pane);
            }
        }
    }

    private TitledPane createPromotionPane(Promotion promo, String query) {
        TilePane tile = new TilePane();
        tile.setHgap(10);
        tile.setVgap(10);

        boolean hasResults = false;

        for (Product product : productFeed.getFilteredProducts()) {
            Double discount = promo.getDiscounts().get(product.getId());

            if (discount != null) {
                if (query.isEmpty() || product.getName().toLowerCase().contains(query.toLowerCase())) {
                    Node card = createProductCard(product, promo);
                    tile.getChildren().add(card);
                    hasResults = true;
                }
            }
        }

        if (!hasResults) return null;

        TitledPane pane = new TitledPane();
        pane.setText(promo.getName());
        pane.setContent(tile);

        return pane;
    }

    // --- CART SIDEBAR ---
    private void setupCartSidebar() {
        cartOverlay.widthProperty().bind(root.widthProperty());
        cartOverlay.heightProperty().bind(root.heightProperty());

        cartSidebar.setVisible(false);
        cartSidebar.setManaged(false);
        cartSidebar.setTranslateX(300);

        cartSidebarController.getCloseButton().setOnAction(e -> toggleCart());
        cartSidebarController.getCheckoutButton().setOnAction(e -> SceneHelper.switchScene("dashboard/commercial/checkout.fxml"));
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