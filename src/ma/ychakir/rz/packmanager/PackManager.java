package ma.ychakir.rz.packmanager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import ma.ychakir.rz.packmanager.Controllers.Controller;


public class PackManager extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            Controller.setStage(primaryStage);
            primaryStage.getIcons().add(new Image(getClass().getResource("Views/img/Icon.png").toExternalForm()));
            Font.loadFont(getClass().getResource("Views/fonts/fontawesome-webfont.ttf").toExternalForm(), 12);
            Font.loadFont(getClass().getResource("Views/fonts/Comfortaa-Regular.ttf").toExternalForm(), 12);
            Parent root = FXMLLoader.load(getClass().getResource("Views/main.fxml"));
            primaryStage.setTitle("Pack Manager");
            primaryStage.setScene(new Scene(root, 700, 400));
            primaryStage.show();
        } catch (Exception ex) {
            Alert dialog = new Alert(Alert.AlertType.ERROR);
            dialog.setTitle("Error.");
            dialog.setContentText(ex.getMessage());
            dialog.showAndWait();
        }
    }
}
