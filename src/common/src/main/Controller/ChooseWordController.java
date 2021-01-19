package common.src.main.Controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

/**
 * A controller for the screen showed when the player is prompted to choose a word.
 */


public class ChooseWordController {
    @FXML
    Button chooseWordBtn1;

    @FXML
    Button chooseWordBtn2;

    @FXML
    Button chooseWordBtn3;

    public ChooseWordController() {
    }

    public void setupButtons(String[] wordsInfo) {
        chooseWordBtn1.setText(wordsInfo[0]);
        chooseWordBtn2.setText(wordsInfo[1]);
        chooseWordBtn3.setText(wordsInfo[2]);
    }

    public void setEventHandler(EventHandler<ActionEvent> eventHandler) {
        chooseWordBtn1.setOnAction(eventHandler);
        chooseWordBtn2.setOnAction(eventHandler);
        chooseWordBtn3.setOnAction(eventHandler);
    }
}
