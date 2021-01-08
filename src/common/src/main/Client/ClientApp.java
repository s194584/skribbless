package common.src.main.Client;

import common.src.main.StartController;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jspace.ActualField;
import org.jspace.SequentialSpace;
import org.jspace.Space;

public class ClientApp extends Application {

    public static void main(String[] args) {launch(args);}

    @Override
    public void start(Stage stage) throws Exception {
        Space space = new SequentialSpace();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("start.fxml"));
        fxmlLoader.setController(new StartController());
        Parent root = fxmlLoader.load();

        space.getp(new ActualField(" "));

        stage.setScene(new Scene(root));
        stage.show();

    }
}
