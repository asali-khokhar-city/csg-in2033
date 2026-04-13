package ac.csg.pu.gui.dashboard.commercial;

import ac.csg.pu.config.Constants;
import ac.csg.pu.gui.SceneHelper;
import ac.csg.pu.gui.util.SessionManager;
import ac.csg.pu.gui.util.ShakeAnimation;
import ac.csg.pu.members.UserDatabase;
import ac.csg.pu.ord.OrderDatabase;
import ac.csg.pu.ord.OrderStatus;
import ac.csg.pu.sales.Cart;
import ac.csg.pu.sales.CartItem;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.time.LocalDate;

public class CheckoutController {

    @FXML private VBox summaryBox;
    @FXML private Label totalLabel;
    @FXML private TextField nameField;
    @FXML private TextField cardNumberField;
    @FXML private TextField expiryField;
    @FXML private TextField cvvField;
    @FXML private TextField addressField;
    @FXML private TextField guestEmailField;
    @FXML private Button payButton;
    @FXML private Button exitButton;
    @FXML private Label messageLabel;

    @FXML
    public void initialize() {
        refreshSummary();

        payButton.setOnAction(e -> processPayment());
        exitButton.setOnAction(e -> returnHome());

        boolean isGuest = SessionManager.User.isGuest();
        guestEmailField.setVisible(isGuest);
        guestEmailField.setManaged(isGuest);
    }

    private void refreshSummary() {
        summaryBox.getChildren().clear();
        for (CartItem item : Cart.getItems().values()) {
            String promoText = item.getPromotion() != null ? " (" + item.getPromotion().getName() + ")" : "";
            Text itemText = new Text(item.getProduct().getName() + promoText + " x" + item.getQuantity() +
                    " - £" + String.format("%.2f", item.getTotalPrice()));
            summaryBox.getChildren().add(itemText);
        }


        double total = Cart.getTotalPrice();
        boolean isTenth = UserDatabase.isTenthOrder(SessionManager.User.getEmail());

        if (isTenth) {
            Text discountText = new Text("🎉 10th purchase discount: -£" + String.format("%.2f", total * 0.1));
            summaryBox.getChildren().add(discountText);
            total = total * Constants.TENTH_ORDER_DISCOUNT;
        }

        totalLabel.setText("Total: £" + String.format("%.2f", total));
    }

    @FXML
    private boolean validatePaymentInfo() {
        String name = nameField.getText().trim();
        String number = cardNumberField.getText().replaceAll("\\s+", "");
        String expiry = expiryField.getText().trim();
        String cvv = cvvField.getText().trim();
        String address = addressField.getText().trim();

        if (SessionManager.User.isGuest()) {
            String guestEmail = guestEmailField.getText().trim();
            SessionManager.User.setGuestEmail(guestEmail);
        }

        // Basic client-side validation
        if (name.isEmpty() || number.isEmpty() || expiry.isEmpty() || cvv.isEmpty() || address.isEmpty()) {
            messageLabel.setText("All fields are required.");
            shakeFields();
            return false;
        }

        if (SessionManager.User.isGuest() && !SessionManager.User.hasGuestEmail()) {
            messageLabel.setText("Email is required.");
            shakeFields();
            return false;
        }

        if (!number.matches("\\d{12,19}")) {
            messageLabel.setText("Card number must be numeric (12–19 digits).");
            shakeFields();
            return false;
        }

        if (!expiry.matches("\\d{2}/\\d{2}")) {
            messageLabel.setText("Expiry must be MM/YY format.");
            shakeFields();
            return false;
        }

        if (!cvv.matches("\\d{3,4}")) {
            messageLabel.setText("CVV must be 3 or 4 digits.");
            shakeFields();
            return false;
        }

        return true;
    }

    private void returnHome() {
        SceneHelper.switchScene("dashboard/commercial/home.fxml");
    }

    private void processPayment() {
        if (!validatePaymentInfo()) return;
        saveTransaction();
    }

    private void setInputsDisabled(boolean disabled) {
        nameField.setDisable(disabled);
        cardNumberField.setDisable(disabled);
        expiryField.setDisable(disabled);
        cvvField.setDisable(disabled);
        addressField.setDisable(disabled);
        payButton.setDisable(disabled);
    }

    private void saveTransaction() {
        String address = addressField.getText().trim();
        String customerEmail = SessionManager.User.isGuest()
                ? SessionManager.User.getGuestEmail()
                : SessionManager.User.getEmail();

        boolean isTenth = !SessionManager.User.isGuest() &&
                UserDatabase.isTenthOrder(customerEmail);

        int orderId = OrderDatabase.insertOrder(
                customerEmail,
                OrderStatus.ACCEPTED.name(),
                LocalDate.now().toString(),
                address
        );

        if (orderId == -1) {
            messageLabel.setText("Failed to save order.");
            return;
        }

        for (CartItem item : Cart.getItems().values()) {
            double unitPrice = isTenth
                    ? item.getUnitPrice() * Constants.TENTH_ORDER_DISCOUNT
                    : item.getUnitPrice();

            OrderDatabase.insertItem(
                    orderId,
                    item.getProduct().getId(),
                    item.getProduct().getName(),
                    unitPrice,
                    item.getQuantity()
            );
        }

        if (!SessionManager.User.isGuest()) {
            UserDatabase.incrementPurchase(customerEmail);
        }

        Cart.clear();
        setInputsDisabled(true);
        messageLabel.setText("Payment successful! Order #" + orderId + " placed.");
        refreshSummary();
    }

    private void shakeFields() {
        ShakeAnimation.shake(nameField);
        ShakeAnimation.shake(cardNumberField);
        ShakeAnimation.shake(expiryField);
        ShakeAnimation.shake(cvvField);
        ShakeAnimation.shake(addressField);
    }
}