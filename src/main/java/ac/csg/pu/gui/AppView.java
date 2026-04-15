package ac.csg.pu.gui;

import ac.csg.pu.comms.RestServer;
import ac.csg.pu.members.UserDatabase;
import ac.csg.pu.ord.OrderDatabase;
import ac.csg.pu.prm.PromotionDatabase;
import ac.csg.pu.test.TestDataInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AppView extends Application {

    @Override
    public void init() {
        UserDatabase.createTable();
        PromotionDatabase.createTables();
        OrderDatabase.createTables();
        TestDataInitializer.init();
    }

    @Override
    public void start(Stage stage) throws IOException {
        SceneHelper.setStage(stage);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("auth/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
