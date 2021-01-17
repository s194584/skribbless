package common.src.main.Client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientApp extends Application {
    //--module-path ${JAVAFX_11} --add-modules=javafx.fxml,javafx.controls
    public static void main(String[] args) {launch(args);}

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/start.fxml"));
        fxmlLoader.setController(new StartController());
        Parent root = fxmlLoader.load();
        stage.setScene(new Scene(root));
        stage.sizeToScene();
        stage.show();
        stage.setResizable(false);
    }
}
