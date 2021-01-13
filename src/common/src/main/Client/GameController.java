package common.src.main.Client;


import common.src.main.Enum.RoomFlag;
import common.src.main.Enum.RoomResponseFlag;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.jspace.SequentialSpace;
import org.jspace.Space;

import java.io.IOException;

public class GameController {

    private Pane root;
    private TaskInfo taskInfo;
    private int playerID;
    @FXML
    Pane gamePane;
    @FXML
    ListView userListView;
    @FXML
    TextFlow chatTextFlow;
    @FXML
    TextField chatTextField;
    @FXML
    ScrollPane chatScrollPane;
    @FXML
    Label timeLabel;
    @FXML
    Pane canvasPaneRoot;
    @FXML
    Label roundsLeftLabel;


    private ObservableList<User> users = FXCollections.observableArrayList();

    private SimpleObjectProperty sop;
    private SimpleStringProperty ssp;
    private SimpleBooleanProperty isLeader;
    private boolean gameStarted = false;
    private Space ui;
    private Parent chooseWordPane;
    private ChooseWordController cwCon;
    private boolean myTurn;

    public GameController(Pane r, TaskInfo ti) {
        root = r;
        taskInfo = ti;
        ui = new SequentialSpace();
        sop = new SimpleObjectProperty();
        ssp = new SimpleStringProperty();
        isLeader = new SimpleBooleanProperty(false);
        playerID = taskInfo.getUserID();
    }

    @FXML
    public void initialize() {
        //make chooseWord loader:
        setupChooseWord(playerID);


        GameUserTask gut = new GameUserTask(taskInfo, ui);
        ((Stage) root.getScene().getWindow()).setScene(new Scene(gamePane));

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
                        updateChat((TextInfo) message[1]);
                        break;
                    case CANVAS:
                        updateCanvas((GraphicsContext) message[1]); //TODO: does this work?
                        break;
                    case GAMESTART:
                        int gameInfo[] = (int[]) message[1];
                        roundsLeftLabel.setText("" + gameInfo[0]);
                        timeLabel.setText(gameInfo[1] + " s");

                        if (isLeader.getValue()) {
                            canvasPaneRoot.getChildren().clear();
                        }
                        break;
                    case CHOOSEWORD:
                        String wordsInfo[] = (String[]) message[1];
                        showChooseWord(wordsInfo);
                        myTurn = true;
                        break;
                    case STARTTURN:
                        
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
                if (!gameStarted && isLeader.getValue()) { //TODO: This logic might need to be somewhere else. since we don't acces it except in the begining.
                    System.out.println("GameController setting gameOptions");

                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/gameOptions.fxml"));
                    GameOptionsController gameOpCon = new GameOptionsController();
                    fxmlLoader.setController(gameOpCon);

                    try {
                        canvasPaneRoot.getChildren().add(fxmlLoader.load());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    gameOpCon.startGameButton.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            try {
                                int data[] = {(int) gameOpCon.roundsComboBox.getValue(),(int) gameOpCon.timeComboBox.getValue()};
                                ui.put(RoomFlag.GAMESTART,playerID,data);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });


                } else { //TODO: Game is started

                }
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

        //Listener for writing in chat:
        chatTextField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                try {
                    if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                        ui.put(RoomFlag.MESSAGE,playerID,chatTextField.getText());
                        System.out.println("GameController put: " + chatTextField.getText());
                        chatTextField.setText("");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread th = new Thread(gut);
        th.setDaemon(true);
        th.start();
    }

    private void setupChooseWord(int playerID) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/chooseWord.fxml"));
        cwCon = new ChooseWordController();
        fxmlLoader.setController(cwCon);

        try {
            chooseWordPane = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        EventHandler<ActionEvent> eventHandler = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    ui.put(RoomFlag.WORDCHOOSEN,playerID,((Button) actionEvent.getSource()).getText());
                    canvasPaneRoot.getChildren().clear();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        cwCon.setEventHandler(eventHandler);
    }

    private void showChooseWord(String[] wordsInfo) {
        cwCon.setupButtons(wordsInfo);
        canvasPaneRoot.getChildren().clear();
        canvasPaneRoot.getChildren().add(chooseWordPane);
    }

    private void updateCanvas(GraphicsContext graphicsContext) {

    }

    private void updateChat(TextInfo textInfo) {
        if(chatTextFlow.getChildren().size()>3){
            chatTextFlow.getChildren().remove(0,2);
            chatScrollPane.setVvalue(1.0d);
        }
        chatTextFlow.getChildren().add(textInfo.getText());
        chatTextFlow.getChildren().add(new Text(System.lineSeparator()));
        chatScrollPane.setVvalue(1.0d);
    }

    private void removePlayer(User user) {
        users.remove(user);
    }

    private void addNewPlayer(User user) {
        users.add(user);
    }



}
