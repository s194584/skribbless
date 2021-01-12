package common.src.main.Client;


import common.src.main.Enum.RoomResponseFlag;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.util.Callback;
import org.jspace.SequentialSpace;
import org.jspace.Space;

import java.io.IOException;
import java.util.ArrayList;

public class GameController {
    Pane root;
    TaskInfo taskInfo;
    @FXML
    Pane gamePane;
    @FXML
    ListView userListView;

    private ObservableList<User> users = FXCollections.observableArrayList();

    private SimpleObjectProperty sop;
    private SimpleStringProperty ssp;
    private SimpleBooleanProperty isLeader;
    private Space ui;

    public GameController(Pane r, TaskInfo ti) {
        root = r;
        taskInfo = ti;
        ui = new SequentialSpace();
        sop = new SimpleObjectProperty();
        ssp = new SimpleStringProperty();
        isLeader = new SimpleBooleanProperty(false);
    }

    @FXML
    public void initialize() {
        GameUserTask gut = new GameUserTask(taskInfo, ui);
        root.getChildren().remove(0);
        root.getChildren().add(gamePane);
        root.getScene().getWindow().sizeToScene();

        sop.bind(gut.valueProperty());
        ssp.bind(gut.messageProperty());

        sop.addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observableValue, Object oldValue, Object newValue) {
                Object[] message = (Object[]) newValue;

                System.out.println("GameController got flag: " + message[0]);

                switch ((RoomResponseFlag) message[0]) {
                    case NEWPLAYER:
                        addNewPlayer((User) message[1]);
                        break;
                    case PLAYERREMOVED:
                        removePlayer((User) message[1]);
                        break;
                    case MESSAGE:
                        updateChat((Text) message[1]);
                        break;
                    case CANVAS:
                        updateCanvas((GraphicsContext) message[1]); //TODO: does this work?
                        break;
                    default:
                        break;
                }
            }
        });

        // this is a pseudo binding for isLeader
        ssp.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldString, String newString) {
                isLeader.set(newString.equals("" + true));
            }
        });

        // Listerner for users
        userListView.setItems(users);
        userListView.setCellFactory(new Callback<ListView, ListCell>() {
            @Override
            public ListCell call(ListView listView) {
                UserListViewCell ulvc = new UserListViewCell();
                ulvc.prefWidthProperty().bind(userListView.widthProperty().multiply(0.95));
                return ulvc;
            }
        });



        Thread th = new Thread(gut);
        th.setDaemon(true);
        th.start();
    }

    private void updateCanvas(GraphicsContext graphicsContext) {

    }

    private void updateChat(Text text) {

    }

    private void removePlayer(User user) {
        users.remove(user);
    }

    private void addNewPlayer(User user) {
        users.add(user);
    }


}
