package common.src.main.Controller;

import common.src.main.Client.UserTask;
import common.src.main.Enum.ServerFlag;
import common.src.main.Enum.UiFlag;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import org.jspace.SequentialSpace;
import org.jspace.Space;

import java.io.IOException;

/**
 * The initial controller responsible for handling the user inputs in the initial start screen (see start.fxml)
 */

public class StartController {
    @FXML
    Pane root;
    @FXML
    Button enterServerButton;
    @FXML
    private TextField ipTextField;
    @FXML
    private TextField nameTextField;
    @FXML
    private TextField roomNameTextField;
    @FXML
    Label label1;

    Space ui;
    SimpleStringProperty ssp;

    public StartController(){
        ui = new SequentialSpace();
        ssp = new SimpleStringProperty("Not Sync");
    }

    @FXML
    public void initialize() {
        UserTask userTask = new UserTask(ui);
        ssp.bind(userTask.messageProperty());
        ssp.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String tOld, String tNew) {
                switch(tNew){
                    case "CONNECTED":
                        label1.setText(tNew);
                        try {
                            FXMLLoader fxmlLoader = new FXMLLoader();
                            fxmlLoader.setLocation(getClass().getResource("/game.fxml"));
                            fxmlLoader.setController(new GameController(root,userTask.getTaskInfo()));
                            fxmlLoader.load();
                            userTask.cancel();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    case "NOTCONNECTED":
                        label1.setText(tNew);
                        break;
                }
            }
        });
        Thread th = new Thread(userTask);
        th.setDaemon(true);
        th.start();
    }

    @FXML
    void joinServer () throws InterruptedException {
        if(ipTextField.getText().isEmpty()||nameTextField.getText().isEmpty()||roomNameTextField.getText().isEmpty()){
            return;
        }
        ui.put(UiFlag.IP,ipTextField.getText());
        ui.put(UiFlag.NAME,nameTextField.getText());
        ui.put(UiFlag.ROOMNAME,roomNameTextField.getText());
        ui.put(UiFlag.ACTION, ServerFlag.JOIN);
    }

    @FXML
    void hostServer () throws InterruptedException {
        if(ipTextField.getText().isEmpty()||nameTextField.getText().isEmpty()){
            return;
        }
        ui.put(UiFlag.IP,ipTextField.getText());
        ui.put(UiFlag.NAME,nameTextField.getText());
        ui.put(UiFlag.ROOMNAME,"");
        ui.put(UiFlag.ACTION, ServerFlag.HOST);
    }

}
