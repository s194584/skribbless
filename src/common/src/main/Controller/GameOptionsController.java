package common.src.main.Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

/**
 * A controller for the screen showed when the player is prompted to choose the game options
 * Note this is only displayed to the player with the isLeader tag set to true
 */

public class GameOptionsController {
    private ObservableList<Integer> roundsList = FXCollections.observableArrayList(2, 3, 4, 5, 6, 7, 8);
    private ObservableList<Integer> timeList = FXCollections.observableArrayList(30, 45, 60, 90, 120);

    //Game options
    @FXML
    ComboBox roundsComboBox;
    @FXML
    ComboBox timeComboBox;
    @FXML
    Button startGameButton;

    public GameOptionsController() {

    }

    @FXML
    public void initialize() {
        roundsComboBox.setItems(roundsList);
        roundsComboBox.setValue(roundsList.get(2));

        timeComboBox.setItems(timeList);
        timeComboBox.setValue(timeList.get(2));
    }


}
