package common.src.main.Client;

import common.src.main.DataTransfer.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.Pane;

import java.io.IOException;

/**
 * This is a pure UI class used to fill up the ListView
 */

public class UserListViewCell extends ListCell<User> {

    @FXML
    private Label characterLabel;

    @FXML
    private Label nameLabel;

    @FXML
    private Label pointLabel;

    @FXML
    private Pane root;


    private FXMLLoader fxmlLoader;

    @Override
    protected void updateItem(User user, boolean b) {
        super.updateItem(user, b);

        setText(null);

        // Return if there is no user or the item is empty
        if (user == null || b) {
            setGraphic(null);
            return;
        }

        // Load usercard if it is the first time loading
        if (fxmlLoader == null) {
            try {
                fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("/usercard.fxml"));
                fxmlLoader.setController(this);
                fxmlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Set the ui for the card
        characterLabel.setText(user.getCharacter());
        nameLabel.setText(user.getName());
        pointLabel.setText("" + user.getScore());

        // Set the cell graphic to root pane
        setGraphic(root);
    }
}
