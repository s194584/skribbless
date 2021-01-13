package common.src.main.Client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;

import java.io.IOException;

public class GameOptionsController {
    private ObservableList<Integer> roundsList = FXCollections.observableArrayList(2,3,4,5,6,7,8);
    private ObservableList<Integer> timeList = FXCollections.observableArrayList(30,45,60,90,120);


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
