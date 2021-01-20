package common.src.main.Controller;


import common.src.main.Client.ColorMap;
import common.src.main.Client.GameUserTask;
import common.src.main.Client.UserListViewCell;
import common.src.main.DataTransfer.MouseInfo;
import common.src.main.DataTransfer.TaskInfo;
import common.src.main.DataTransfer.TextInfo;
import common.src.main.DataTransfer.User;
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
import org.jspace.Space;

import java.io.IOException;

/**
 * The GameController is responsible for handling all ui updates during the game, and sending important updates
 * to the lobby space so the room can make decisions on the user input.
 */

public class GameController {
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

    // Canvas variables
    GraphicsContext gc;
    private MouseInfo mouseInfo; // The data transfer object that put in the lobby when the user releases then mouse.
    double prevX, prevY; // Previous mouse position.
    private boolean dragging;

    // GameUserTask variables
    private GameUserTask gut;
    private SimpleObjectProperty sop; // Binding object for the value binding
    private SimpleStringProperty ssp; // Binding object for the message binding

    // JavaFX panes
    private Pane root;
    private Parent chooseWordPane;

    // Controllers
    private ChooseWordController cwCon;
    private GameOptionsController gameOpCon;

    // lobby
    private Space lobby;

    // data objects
    private ObservableList<User> users = FXCollections.observableArrayList();
    private SimpleBooleanProperty isLeader;
    private boolean gameStarted = false;
    private TaskInfo taskInfo;
    private int playerID;
    private boolean myTurn;


    public GameController(Pane r, TaskInfo ti) {
        root = r;
        taskInfo = ti;
        lobby = ti.getLobby();
        sop = new SimpleObjectProperty();
        ssp = new SimpleStringProperty();
        isLeader = new SimpleBooleanProperty(false);
        playerID = taskInfo.getUserID();
    }

    @FXML
    public void initialize() {
        // Starts readies the Game thread
        gut = new GameUserTask(taskInfo);

        // Initial window setup
        ((Stage) root.getScene().getWindow()).setOnCloseRequest(windowEvent -> gut.cancel());
        ((Stage) root.getScene().getWindow()).setTitle(taskInfo.getHostPort());
        ((Stage) root.getScene().getWindow()).setScene(new Scene(gamePane));
        userListView.setMouseTransparent(true);
        userListView.setFocusTraversable(false);

        // Make chooseWord loader:
        setupChooseWord(playerID);
        setupCanvas();

        // Setting up color box.
        ObservableList<String> colorsList = ColorMap.getColorList();
        colorComboBox.setItems(colorsList);
        colorComboBox.setValue(colorsList.get(0));

        // Create bindings and their listeners
        sop.bind(gut.valueProperty());
        ssp.bind(gut.messageProperty());

        sop.addListener(this::handelInputs);

        // the message binding is a pseudo binding for isLeader
        ssp.addListener(this::isLeaderBinding);

        /* Listeners for users
         Ensures the listview is updated along with the ObservableList of users*/
        userListView.setItems(users);
        userListView.setCellFactory((Callback<ListView, ListCell>) listView -> {
            UserListViewCell ulvc = new UserListViewCell();
            ulvc.prefWidthProperty().bind(userListView.widthProperty().multiply(0.95));
            return ulvc;
        });

        //Listener for writing in chat:
        chatTextField.setOnKeyPressed(keyEvent -> {
            try {
                if (keyEvent.getCode().equals(KeyCode.ENTER) && !chatTextField.getText().isEmpty()) {
                    // Broadcast any messages that is not an empty string.
                    lobby.put(RoomFlag.MESSAGE, playerID, chatTextField.getText());
                    chatTextField.setText("");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        // Start GameUserTask thread.
        Thread th = new Thread(gut);
        th.setDaemon(true);
        th.start();
    }

    // This method handles all input to the inbox from the Room.
    private void handelInputs(Object observableValue, Object oldValue, Object newValue) {
        Object[] message = (Object[]) newValue;
        RoomResponseFlag flag = (RoomResponseFlag) message[0];
        Object data = message[1];

        switch (flag) {
            case NEWPLAYER: // Add player and potentially start gameOptions
                addNewPlayers((User[]) data);
                if (isLeader.getValue() && users.size() > 1) {
                    gameOpCon.startGameButton.setDisable(false);
                }
                break;
            case PLAYERREMOVED: // Remove player and potentially disable being able to start the game
                removePlayer((User) data);
                if (gameOpCon != null && users.size() < 2) {
                    gameOpCon.startGameButton.setDisable(true);
                }
                break;
            case MESSAGE: // Update the chat
                updateChat((TextInfo) data);
                break;
            case CANVAS: // Update the canvas
                // This can be a heavy task so runLater is utilized.
                Platform.runLater(() -> updateCanvas((MouseInfo) data));
                break;
            case GAMESTART: // Set initial data and wait for either chooseword or startturn
                int[] gameInfo = (int[]) data;
                roundsLeftLabel.setText("" + gameInfo[0]);
                timeLabel.setText(gameInfo[1] + "s");
                if (isLeader.getValue()) {
                    canvasPaneRoot.getChildren().clear();
                }
                break;
            case CHOOSEWORD: // Prompts user to choose a word
                String[] wordsInfo = (String[]) data;
                showChooseWord(wordsInfo);
                break;
            case STARTTURN: // Start a new turn depending on if you are drawing or guessing
                int[] wordAndId = (int[]) data;
                myTurn = wordAndId[1] == playerID;
                if (!myTurn) {
                    currentWordLabel.setText("_ ".repeat(wordAndId[0]));
                    canvas.setMouseTransparent(true);
                }else{
                    canvas.setMouseTransparent(false);
                }
                // Show whose turn it is
                userListView.getSelectionModel().select(new User("", "", wordAndId[1], 0));

                canvasPaneRoot.getChildren().clear();
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                canvasPaneRoot.getChildren().add(canvas);
                break;
            case TIMETICK: // Update time
                timeLabel.setText(data + "s");
                break;
            case NEXTROUND: // Update roundsleft
                roundsLeftLabel.setText(data + "");
                break;
            case ADDPOINTS: // Update users
                addPoints((User[]) data);
                userListView.refresh();
                break;
            case STOPDRAW: // disable more drawing inputs from the user
                myTurn = false;
                canvas.setMouseTransparent(true);

                break;
            case ENDGAME: // Display end screen
                User[] rankedUsers = (User[]) data;
                userListView.getParent().setVisible(false);
                users.clear();
                users.addAll(rankedUsers);
                setupGameOver();
                break;
            case ROOMNAME:
                // Update window title to room's name
                ((Stage) canvasPaneRoot.getScene().getWindow()).setTitle(data.toString());
                break;
            default:
                break;
        }
    }

    // Binding for message value.
    private void isLeaderBinding(Object observableValue, Object oldValue, Object newValue) {
        isLeader.set(newValue.equals("" + true));
        if (!gameStarted && isLeader.getValue()) {

            // Loading GameOptions pane
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/gameOptions.fxml"));
            gameOpCon = new GameOptionsController();
            fxmlLoader.setController(gameOpCon);

            try {
                canvasPaneRoot.getChildren().add(fxmlLoader.load());
            } catch (IOException e) {
                e.printStackTrace();
            }

            // GAMESTART button setup
            gameOpCon.startGameButton.setDisable(true);
            gameOpCon.startGameButton.setOnAction(actionEvent -> {
                try {
                    // Get important data and put to lobby
                    int data[] = {(int) gameOpCon.roundsComboBox.getValue(), (int) gameOpCon.timeComboBox.getValue()};
                    lobby.put(RoomFlag.GAMESTART, playerID, data);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void addPoints(User[] data) {
        // Incoming users
        for (User u1 : data) {
            // List of all users
            for (User u2 : users) {
                if (u1.equals(u2)) {
                    u2.setScore(u1.getScore());
                }
            }
        }
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
                lobby.put(RoomFlag.WORDCHOSEN, playerID, tmp);
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

    private void updateChat(TextInfo textInfo) {
        // Removes old chat messages when a total of 15 are reached.
        if (chatTextFlow.getChildren().size() > 30) {
            chatTextFlow.getChildren().remove(0, 2);
            chatScrollPane.setVvalue(1.0d);
        }
        chatTextFlow.getChildren().add(textInfo.getText());
        chatTextFlow.getChildren().add(new Text(System.lineSeparator()));
        chatScrollPane.setVvalue(1.0d);
    }

    private void addNewPlayers(User[] data) {
        users.clear();
        users.addAll(data);
    }

    private void removePlayer(User user) {
        users.remove(user);
    }

    // Click event for the quit button. Closes application and start it anew.
    @FXML
    void quit(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/start.fxml"));
        fxmlLoader.setController(new StartController());
        ((Stage) canvasPaneRoot.getScene().getWindow()).setScene(new Scene(fxmlLoader.load()));
        gut.cancel();
    }

    /*
     *
     *   The remaining methods are used to update the canvas and set it up when prompted.
     *   It also keeps track of mouse positions and puts it in the lobby when the player finished a stroke
     *
     */

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
        for (double[] pos : mi.getLines()) {
            draw(pos[0], pos[1], pos[2], pos[3], mi.getCt(), mi.getCc());
        }
    }

    private void draw(double x1, double y1, double x2, double y2, CanvasTool ct, CanvasColor cc) {
        gc.setStroke(ColorMap.getColor(cc));
        switch (ct) {
            case PENCIL:
                gc.strokeLine(x1, y1, x2, y2);
                break;
        }
    }

    @FXML
    void releaseTool(MouseEvent event) {
        // myTurn used to block strokes that start before STOPDRAW is received and stops after
        if (myTurn) {
            dragging = false;
            try {
                lobby.put(RoomFlag.CANVAS, playerID, mouseInfo); // put mouseInfo so other clients can update ui
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    @FXML
    void updateInitialPosition(MouseEvent event) {
            mouseInfo = new MouseInfo(CanvasTool.PENCIL, CanvasColor.valueOf(colorComboBox.getValue().toString()));
            prevX = event.getX();
            prevY = event.getY();
            dragging = true;
    }

    @FXML
    void updateStroke(MouseEvent event) {
        if (!dragging) {
            return;
        }
        double x = event.getX();
        double y = event.getY();

        // This is responsible for only making strokes between every third pixel in both x and y direction.
        // Made to limit the amount of data parsed on.
        if (Math.abs(x - prevX) < 3 && Math.abs(y - prevY) < 3) {
            return;
        }
        // Drawing on own canvas
        gc.setStroke(ColorMap.getColor(mouseInfo.getCc()));
        gc.strokeLine(prevX, prevY, event.getX(), event.getY());
        // Packaging the stroke
        mouseInfo.addLine(prevX, prevY, event.getX(), event.getY());

        prevX = event.getX();
        prevY = event.getY();
    }

}
