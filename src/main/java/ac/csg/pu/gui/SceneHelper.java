package ac.csg.pu.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneHelper {

    private static Stage primaryStage;

    public static void setStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        if (primaryStage == null) {
            throw new IllegalStateException("Primary stage not set. Call SceneHelper.setStage(stage) first.");
        }
        return primaryStage;
    }

    public static void switchScene(String fxmlPath) {
        Stage stage = getPrimaryStage();
        try {
            // Remember current size so the window doesn't resize on scene change
            double currentWidth  = stage.getWidth();
            double currentHeight = stage.getHeight();

            Parent root = FXMLLoader.load(SceneHelper.class.getResource(fxmlPath));
            Scene scene = new Scene(root);
            stage.setScene(scene);

            // Restore the same dimensions instead of shrinking/growing
            stage.setWidth(currentWidth);
            stage.setHeight(currentHeight);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load FXML: " + fxmlPath, e);
        }
    }

    public static Stage getStage(javafx.scene.Node node) {
        if (node == null || node.getScene() == null) {
            throw new IllegalArgumentException("Node is not attached to a scene yet.");
        }
        return (Stage) node.getScene().getWindow();
    }
}
