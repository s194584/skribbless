package common.src.main.Client;


import common.src.main.Enum.CanvasColor;
import common.src.main.Enum.CanvasTool;
import common.src.main.Enum.RoomFlag;
import common.src.main.Enum.RoomResponseFlag;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
    Button quitButton;
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
    private GameUserTask gut;
    private SimpleObjectProperty sop;
    private SimpleStringProperty ssp;
    private SimpleBooleanProperty isLeader;
    private boolean gameStarted = false;
    private Space ui;
    private boolean dragging;
    private GameOptionsController gameOpCon;
    private Parent chooseWordPane;
    private ChooseWordController cwCon;
    private boolean myTurn;

    private MouseInfo mouseInfo;



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
        gut = new GameUserTask(taskInfo, ui);

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
                    addNewPlayers((User[]) data);
                    if(isLeader.getValue()&&users.size()>1){
                        gameOpCon.startGameButton.setDisable(false);
                    }
                    break;
                case PLAYERREMOVED:
                    removePlayer((User) data);
                    if(gameOpCon!=null&&users.size()<2){
                        gameOpCon.startGameButton.setDisable(true);
                    }
                    break;
                case MESSAGE:
                    updateChat((TextInfo) data);
                    break;
                case CANVAS:
                    Platform.runLater(() -> updateCanvas((MouseInfo) data));
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
                    // Show whose turn it is
                    userListView.getSelectionModel().select(new User("","",wordAndId[1],0));

                    canvasPaneRoot.getChildren().clear();
                    gc.clearRect(0,0,canvas.getWidth(),canvas.getHeight());
                    canvasPaneRoot.getChildren().add(canvas);
                    break;
                case TIMETICK:
                    timeLabel.setText(data+"s");
                    break;
                case NEXTROUND:
                    roundsLeftLabel.setText(data+"");
                    break;
                case ADDPOINTS:
                    addPoints((User[])data);
                    userListView.refresh();
                    break;
                case STOPDRAW:
                    myTurn = false;
                    break;
                case ENDGAME:
                    User[] rankedUsers = (User[]) data;
                    userListView.getParent().setVisible(false);
                    users.clear();
                    users.addAll(rankedUsers);
                    setupGameOver();
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
                gameOpCon = new GameOptionsController();
                fxmlLoader.setController(gameOpCon);

                try {
                    canvasPaneRoot.getChildren().add(fxmlLoader.load());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                gameOpCon.startGameButton.setDisable(true);

                gameOpCon.startGameButton.setOnAction(actionEvent -> {
                    try {
                        int data[] = {(int) gameOpCon.roundsComboBox.getValue(), (int) gameOpCon.timeComboBox.getValue()};
                        ui.put(RoomFlag.GAMESTART, playerID, data);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
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

    private void addNewPlayers(User[] data) {
        users.clear();
        users.addAll(data);
    }

    private void setupGameOver() {
        ScrollPane gameOverScrollPane = new ScrollPane();
        ListView ranksListView = new ListView();
        ranksListView.setCellFactory((Callback<ListView, ListCell>) listView -> new UserListViewCell());
        ranksListView.setItems(users);
        ranksListView.setMouseTransparent(true);
        gameOverScrollPane.setContent(ranksListView);

        canvasPaneRoot.getChildren().clear();
        canvasPaneRoot.getChildren().add(gameOverScrollPane);
    }

    private void addPoints(User[] data) {
        // Incoming users
        for (User u1 : data) {
            // List of all users
            for (User u2: users) {
                if (u1.equals(u2)){
                    u2.setScore(u1.getScore());
                }
            }
        }
    }

    private void setupCanvas() {
        canvas = new Canvas();
        canvas.setWidth(814);
        canvas.setHeight(436);
        canvas.setOnMousePressed(this::updateInitialPosition);
        canvas.setOnMouseDragged(this::updateStroke);
        try {
            canvas.setOnMouseReleased(this::releaseTool);
        } catch (Exception e) {
            e.printStackTrace();
        }
        gc = canvas.getGraphicsContext2D();
    }

    private void updateCanvas(MouseInfo mi) {
        for (double[] pos: mi.getLines()) {
            draw(pos[0],pos[1],pos[2],pos[3],mi.getCt(),mi.getCc());
        }
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


        cwCon.setEventHandler(actionEvent -> {
            try {
                String tmp = ((Button) actionEvent.getSource()).getText();
                // Send WordChosen
                ui.put(RoomFlag.WORDCHOSEN,playerID,tmp);
                currentWordLabel.setText(tmp);
                canvasPaneRoot.getChildren().clear();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
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

    @FXML
    void releaseTool(MouseEvent event) {
        dragging = false;
        try {
            ui.put(RoomFlag.CANVAS,playerID,mouseInfo);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void updateInitialPosition(MouseEvent event) {
        if (myTurn) {
            mouseInfo = new MouseInfo(CanvasTool.PENCIL,CanvasColor.valueOf(colorComboBox.getValue().toString()));
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
        if (Math.abs(x-prevX)<3&&Math.abs(y-prevY)<3){
            return;
        }
        // TODO: Maybe do some chunking, if more disappears under transport

        gc.setStroke(ColorMap.getColor(mouseInfo.getCc()));
        gc.strokeLine(prevX,prevY,event.getX(),event.getY());
        mouseInfo.addLine(prevX,prevY,event.getX(),event.getY());
        prevX = event.getX();
        prevY = event.getY();
    }

    @FXML
    void quit(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/start.fxml"));
        fxmlLoader.setController(new StartController());
        ((Stage)canvasPaneRoot.getScene().getWindow()).setScene(new Scene(fxmlLoader.load()));
        gut.cancel();
    }

}
