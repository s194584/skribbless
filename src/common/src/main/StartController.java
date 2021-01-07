package common.src.main;

import javafx.application.Platform;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.jspace.FormalField;
import org.jspace.Space;

public class StartController {
    private Space space;
    static int update = 0;
    @FXML
    private Label label;

    private Task task;

    public StartController(Space s){
        space = s;
    }

    @FXML
    public void initialize(){
        task = new Task<Integer>() {
            public SimpleStringProperty labelString = new SimpleStringProperty("Start");
            @Override
            protected Integer call() throws Exception {
                while(!isCancelled()) {
                    Object[] t = space.getp(new FormalField(String.class));

                    
                    if (t==null){
                        continue;
                    }
                    updateMessage(t[0].toString());
                }
                return 1;
            }
        };
        System.out.println("Initcontroller");
        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();

        label.textProperty().bind(task.messageProperty());
        System.out.println("Thread activated");

    }

    @FXML
    void putIntoInbox(ActionEvent event) throws InterruptedException {
        System.out.println("added update: "+update);
        space.put("Somestrings"+update);
        update++;

    }

    @FXML
    void syncInterface() throws InterruptedException {
        System.out.println("Starting Thread...");


//        new Thread(new Syncer(labelString,space)).start();
        System.out.println("Thread running...");
    }

}
