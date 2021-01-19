package common.src.main.Client;

import common.src.main.Enum.CanvasColor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

import java.util.Map;

/**
 * This class is responsible for mapping the colors of the enum CanvasColor to the javafx color objects.
 * The reason for this is that javafx objects cannot be serialized.
 */

public class ColorMap {

    private static Map<CanvasColor, Color> colorMap = Map.of(
            CanvasColor.BLACK, Color.BLACK,
            CanvasColor.RED, Color.RED,
            CanvasColor.BLUE, Color.BLUE,
            CanvasColor.YELLOW, Color.YELLOW,
            CanvasColor.GREEN, Color.GREEN,
            CanvasColor.BROWN, Color.BROWN,
            CanvasColor.WHITE, Color.WHITE,
            CanvasColor.PINK, Color.PINK,
            CanvasColor.ORANGE, Color.ORANGE
    );

    // Return list for the ComboBox used in GameController.
    public static ObservableList<String> getColorList() {
        String[] tmp = new String[CanvasColor.values().length];
        int count = 0;
        for (CanvasColor cv : CanvasColor.values()) {
            tmp[count] = cv.toString();
            count++;
        }
        return FXCollections.observableArrayList(tmp);
    }

    public static Color getColor(CanvasColor c) {
        return colorMap.get(c);
    }

}
