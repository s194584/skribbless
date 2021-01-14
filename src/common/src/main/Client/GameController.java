package common.src.main.Client;


import common.src.main.Enum.CanvasColor;
import common.src.main.Enum.CanvasTool;
import common.src.main.Enum.RoomFlag;
import common.src.main.Enum.RoomResponseFlag;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.jspace.SequentialSpace;
import org.jspace.Space;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

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
    Label currentWordLabel;
    @FXML
    Label timeLabel;
    @FXML
    ComboBox colorComboBox;
    @FXML
    Pane canvasPaneRoot;
    @FXML
    Label roundsLeftLabel;
    @FXML
    Canvas canvas;

    GraphicsContext gc;

    double prevX,prevY;

    private ObservableList<User> users = FXCollections.observableArrayList();

    private SimpleObjectProperty sop;
    private SimpleStringProperty ssp;
    private SimpleBooleanProperty isLeader;
    private boolean gameStarted = false;
    private Space ui;
    private boolean dragging;
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
        // Starts readies the Game thread
        GameUserTask gut = new GameUserTask(taskInfo, ui);

        // Initial window setup
        ((Stage) root.getScene().getWindow()).setOnCloseRequest(windowEvent -> gut.cancel());
        ((Stage) root.getScene().getWindow()).setTitle(taskInfo.getHostPort());
        ((Stage) root.getScene().getWindow()).setScene(new Scene(gamePane));
        userListView.setMouseTransparent(true);
        userListView.setFocusTraversable(false);


        //make chooseWord loader:
        setupChooseWord(playerID);
        setupCanvas();

        //Setting up color box.
        ObservableList<String> colorsList = ColorMap.getColorList();
        colorComboBox.setItems(colorsList);
        colorComboBox.setValue(colorsList.get(0));


        sop.bind(gut.valueProperty());
        ssp.bind(gut.messageProperty());

        sop.addListener((observableValue, oldValue, newValue) -> {
            Object[] message = (Object[]) newValue;
            RoomResponseFlag flag = (RoomResponseFlag) message[0];
            Object data = message[1];
            System.out.println("GameController got flag: " + flag);

            switch (flag) {
                case NEWPLAYER:
                    addNewPlayer((User) data);
                    break;
                case PLAYERREMOVED:
                    removePlayer((User) data);
                    break;
                case MESSAGE:
                    updateChat((TextInfo) data);
                    break;
                case CANVAS:
                    updateCanvas((MouseInfo) data); //TODO: does this work?
                    break;
                case GAMESTART:
                    int[] gameInfo = (int[]) data;
                    roundsLeftLabel.setText("" + gameInfo[0]);
                    timeLabel.setText(gameInfo[1] + " s");
                    if (isLeader.getValue()) {
                        canvasPaneRoot.getChildren().clear();
                    }
                    break;
                case CHOOSEWORD:
                    String[] wordsInfo = (String[]) data;
                    showChooseWord(wordsInfo);
                    break;
                case STARTTURN:
                    int[] wordAndId = (int[]) data;
                    myTurn = wordAndId[1]==playerID;
                    if(!myTurn){
                        currentWordLabel.setText("_ ".repeat(wordAndId[0]));
                    }
                    canvasPaneRoot.getChildren().clear();
                    gc.clearRect(0,0,canvas.getWidth(),canvas.getHeight());
                    canvasPaneRoot.getChildren().add(canvas);
                    break;
                default:
                    break;
            }
        });

        // this is a pseudo binding for isLeader
        ssp.addListener((observableValue, oldValue, newValue) -> {
            isLeader.set(newValue.equals("" + true));
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
                            int data[] = {(int) gameOpCon.roundsComboBox.getValue(), (int) gameOpCon.timeComboBox.getValue()};
                            ui.put(RoomFlag.GAMESTART, playerID, data);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else { //TODO: Game is started

            }
        });

        // Listerner for users
        userListView.setItems(users);
        userListView.setCellFactory((Callback<ListView, ListCell>) listView -> {
            UserListViewCell ulvc = new UserListViewCell();
            ulvc.prefWidthProperty().bind(userListView.widthProperty().multiply(0.95));
            return ulvc;
        });

        //Listener for writing in chat:
        chatTextField.setOnKeyPressed(keyEvent -> {
            try {
                if (keyEvent.getCode().equals(KeyCode.ENTER)&&!chatTextField.getText().isEmpty()) {
                    ui.put(RoomFlag.MESSAGE,playerID,chatTextField.getText());
                    System.out.println("GameController put: " + chatTextField.getText());
                    chatTextField.setText("");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread th = new Thread(gut);
        th.setDaemon(true);
        th.start();
    }

    private void setupCanvas() {
        canvas = new Canvas();
        canvas.setWidth(814);
        canvas.setHeight(436);
        canvas.setOnMousePressed(this::updateInitialPosition);
        canvas.setOnMouseDragged(this::updateStroke);
        canvas.setOnMouseReleased(this::releaseTool);
        gc = canvas.getGraphicsContext2D();
    }

    private void updateCanvas(MouseInfo mi) {
        draw(mi.getX1(),mi.getY1(),mi.getX2(),mi.getY2(),mi.getCt(),mi.getCc());
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

        EventHandler<ActionEvent> eventHandler = actionEvent -> {
            try {
                String tmp = ((Button) actionEvent.getSource()).getText();
                ui.put(RoomFlag.WORDCHOOSEN,playerID,tmp);
                currentWordLabel.setText(tmp);
                canvasPaneRoot.getChildren().clear();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        cwCon.setEventHandler(eventHandler);
    }

    private void showChooseWord(String[] wordsInfo) {
        cwCon.setupButtons(wordsInfo);
        canvasPaneRoot.getChildren().clear();
        canvasPaneRoot.getChildren().add(chooseWordPane);
    }

    private void draw(double x1, double y1, double x2, double y2, CanvasTool ct, CanvasColor cc) {
        gc.setStroke(ColorMap.getColor(cc));
        switch (ct){
            case PENCIL:
                gc.strokeLine(x1,y1,x2,y2);
                break;
        }
    }

    private void updateChat(TextInfo textInfo) {
        if(chatTextFlow.getChildren().size()>15){
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

    @FXML
    void releaseTool(MouseEvent event) {
        dragging = false;
    }

    @FXML
    void updateInitialPosition(MouseEvent event) {
        if (myTurn) {
            prevX = event.getX();
            prevY = event.getY();
            dragging = true;
        }
    }

    @FXML
    void updateStroke(MouseEvent event) {
        if (!dragging){
            return;
        }
        double x = event.getX();
        double y = event.getY();
        try {
            if (Math.abs(x-prevX)<10&&Math.abs(y-prevY)<10){
                return;
            }
            // TODO: Maybe do some chunking, if more disappears under transport
            gc.setStroke(ColorMap.getColor(CanvasColor.valueOf(colorComboBox.getValue().toString())));
            gc.strokeLine(prevX,prevY,event.getX(),event.getY());
            ui.put(RoomFlag.CANVAS,playerID,new MouseInfo(prevX,prevY,x,y, CanvasTool.PENCIL,CanvasColor.valueOf(colorComboBox.getValue().toString())));
            prevX = event.getX();
            prevY = event.getY();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



}
