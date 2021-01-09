package common.src.main.Client;


import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class GameController {
    Pane root;
    TaskInfo taskInfo;
    @FXML
    Pane gamePane;


    public GameController(Pane r, TaskInfo ti){
        root = r;
        taskInfo = ti;
    }

    @FXML
    public void initialize() {
        root.getChildren().remove(0);
        root.getChildren().add(gamePane);

    }



}
