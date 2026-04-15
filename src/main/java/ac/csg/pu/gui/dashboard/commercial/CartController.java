package ac.csg.pu.gui.dashboard.commercial;

import ac.csg.pu.sales.Cart;
import ac.csg.pu.sales.CartItem;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
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

    private final IntegerProperty itemCount = new SimpleIntegerProperty(0);

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

        // removeIf makes targeted removals so the ListView scroll position stays stable
        cartItems.removeIf(item -> !latest.contains(item));

        cartListView.refresh();

        itemCount.set(
            Cart.getItems().values().stream().mapToInt(CartItem::getQuantity).sum()
        );
    }

    public IntegerProperty itemCountProperty() {
        return itemCount;
    }

    public Button getCloseButton()    { return closeButton; }
    public Button getCheckoutButton() { return checkoutButton; }
}
