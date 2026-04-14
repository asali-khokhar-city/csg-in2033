package ac.csg.pu.gui.dashboard.commercial;

import ac.csg.pu.sales.Cart;
import ac.csg.pu.sales.CartItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class CartController {

    @FXML private ListView<CartItem> cartListView;
    @FXML private Button checkoutButton;
    @FXML private Button closeButton;

    private static final ObservableList<CartItem> cartItems = FXCollections.observableArrayList();

    private HomeController homeController;

    @FXML
    public void initialize() {
        cartListView.setItems(cartItems);

        cartListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(CartItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("cart-card.fxml"));
                        VBox card = loader.load();
                        CartCardController controller = loader.getController();
                        controller.setItem(item);
                        controller.setCartController(CartController.this);
                        setGraphic(card);
                    } catch (Exception e) {
                        e.printStackTrace();
                        setGraphic(null);
                    }
                }
            }
        });

        refreshCartList();
    }

    public void refreshCartList() {
        List<CartItem> latest = new ArrayList<>(Cart.getItems().values());

        for (CartItem item : latest) {
            if (!cartItems.contains(item)) cartItems.add(item);
        }

        // removeIf triggers a targeted list change rather than a full replacement,
        // which keeps the ListView scroll position stable
        cartItems.removeIf(item -> !latest.contains(item));

        cartListView.refresh();

        if (homeController != null) homeController.updateCartBadge();
    }

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }

    public Button getCloseButton()    { return this.closeButton; }
    public Button getCheckoutButton() { return this.checkoutButton; }
}
