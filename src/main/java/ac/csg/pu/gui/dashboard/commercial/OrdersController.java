package ac.csg.pu.gui.dashboard.commercial;

import ac.csg.pu.gui.SceneHelper;
import ac.csg.pu.gui.util.SessionManager;
import ac.csg.pu.ord.Order;
import ac.csg.pu.ord.OrderDatabase;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

import java.util.List;

public class OrdersController {

    @FXML private VBox ordersContainer;

    @FXML
    public void initialize() {
        loadOrders();
    }

    private void loadOrders() {
        String email = SessionManager.User.isGuest()
                ? SessionManager.User.getGuestEmail()
                : SessionManager.User.getEmail();

        List<Order> orders = OrderDatabase.getOrders(email);

        ordersContainer.getChildren().clear();

        for (Order order : orders) {
            ordersContainer.getChildren().add(createOrderCard(order));
        }
    }

    private VBox createOrderCard(Order order) {
        return new OrderCardController().create(order);
    }

    @FXML
    private void goBack() {
        SceneHelper.switchScene("dashboard/commercial/home.fxml");
    }
}