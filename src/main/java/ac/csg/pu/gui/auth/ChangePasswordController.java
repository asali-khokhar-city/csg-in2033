package ac.csg.pu.gui.auth;

import ac.csg.pu.gui.SceneHelper;
import ac.csg.pu.gui.util.SessionManager;
import ac.csg.pu.members.UserDatabase;
import ac.csg.pu.members.UserType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

public class ChangePasswordController {

    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;

    @FXML
    private void onSubmit() {
        String newPassword = newPasswordField.getText().trim();
        String confirm = confirmPasswordField.getText().trim();

        if (newPassword.isEmpty() || confirm.isEmpty()) {
            errorLabel.setText("All fields are required.");
            return;
        }

        if (!newPassword.equals(confirm)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }

        if (newPassword.length() < 8) {
            errorLabel.setText("Password must be at least 8 characters.");
            return;
        }

        String email = SessionManager.User.getEmail();
        UserDatabase.changePassword(email, newPassword);
        UserDatabase.setFirstLogin(email, false);

        // Route back to login
        SessionManager.Pending.setMessage("Password changed successfully. Please log in.");
        SessionManager.logout();
        SceneHelper.switchScene("auth/login.fxml");
    }

    public void onCancel(ActionEvent actionEvent) {
        SessionManager.logout();
        SceneHelper.switchScene("auth/login.fxml");
    }
}