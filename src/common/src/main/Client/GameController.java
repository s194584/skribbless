package common.src.main.Client;


import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import org.jspace.SequentialSpace;
import org.jspace.Space;

import java.io.IOException;

public class GameController {
    Pane root;
    TaskInfo taskInfo;
    @FXML
    Pane gamePane;



    SimpleObjectProperty sop;
    Space ui;

    public GameController(Pane r, TaskInfo ti){
        root = r;
        taskInfo = ti;
        ui = new SequentialSpace();
        sop = new SimpleObjectProperty();
    }

    @FXML
    public void initialize() {
        GameUserTask gut = new GameUserTask(taskInfo,ui);
        root.getChildren().remove(0);
        root.getChildren().add(gamePane);
        root.getScene().getWindow().sizeToScene();

    }



}
