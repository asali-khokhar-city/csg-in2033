package ac.csg.pu.gui.dashboard.commercial;

import ac.csg.pu.ord.Order;
import ac.csg.pu.ord.OrderItem;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderCardController {

    @FXML private Label statusLabel;
    @FXML private Label dateLabel;
    @FXML private Label totalLabel;
    @FXML private Label addressLabel;
    @FXML private VBox detailsBox;
    @FXML private VBox itemsBox;
    @FXML private Button toggleButton;

    private boolean expanded = false;

    private static final Logger logger = LoggerFactory.getLogger(OrderCardController.class);

    public VBox create(Order order) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("order-card.fxml"));
            VBox root = loader.load();
            OrderCardController controller = loader.getController();
            controller.bind(order);
            return root;
        } catch (Exception e) {
            e.printStackTrace();
            return new VBox();
        }
    }

    private void bind(Order order) {
        itemsBox.getChildren().clear();

        double total = 0;

        for (OrderItem item : order.getItems()) {
            Label itemLabel = new Label(
                item.productName() + " x" + item.quantity() + " - £" + String.format("%.2f", item.getTotalPrice())
            );
            itemLabel.getStyleClass().add("order-item-label");
            total += item.getTotalPrice();
            itemsBox.getChildren().add(itemLabel);
            logger.info("Rendering product: {}", item.productName());
        }

        logger.info("Item count for order #{}: {}", order.getId(), order.getItems().size());

        statusLabel.setText(order.getStatus().toString());
        dateLabel.setText(order.getDate().toString());
        addressLabel.setText("Delivery: " + order.getAddress());
        totalLabel.setText(String.format("£%.2f", total));

        toggleButton.setOnAction(e -> toggle());
    }

    private void toggle() {
        expanded = !expanded;
        detailsBox.setVisible(expanded);
        detailsBox.setManaged(expanded);
        toggleButton.setText(expanded ? "Hide Items" : "View Items");
    }
}
