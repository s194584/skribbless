package common.src.main;

import common.src.main.Client.GameController;
import common.src.main.Client.UserTask;
import common.src.main.Enum.ServerFlag;
import common.src.main.Enum.UiFlag;
import javafx.application.Platform;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.Space;

import java.io.IOException;


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
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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

//    @FXML
//    public void initialize(){
//        task = new Task<Integer>() {
//            public SimpleStringProperty labelString = new SimpleStringProperty("Start");
//            @Override
//            protected Integer call() throws Exception {
//                while(!isCancelled()) {
//                    Object[] t = space.getp(new FormalField(String.class));
//
//
//                    if (t==null){
//                        continue;
//                    }
//                    updateMessage(t[0].toString());
//                }
//                return 1;
//            }
//        };
//        System.out.println("Initcontroller");
//        Thread th = new Thread(task);
//        th.setDaemon(true);
//        th.start();
//
//        label.textProperty().bind(task.messageProperty());
//        System.out.println("Thread activated");
//
//    }


}
