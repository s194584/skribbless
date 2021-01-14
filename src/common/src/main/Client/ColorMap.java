package common.src.main.Client;

import common.src.main.Enum.CanvasColor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.HashMap;
import java.util.Map;

public class ColorMap {

    private static Map<CanvasColor, Color> colorMap = Map.of(
            CanvasColor.BLACK, Color.BLACK,
            CanvasColor.RED, Color.RED,
            CanvasColor.BLUE, Color.BLUE,
            CanvasColor.YELLOW, Color.YELLOW,
            CanvasColor.GREEN, Color.GREEN,
            CanvasColor.BROWN, Color.BROWN,
            CanvasColor.WHITE, Color.WHITE
    );

    public static ObservableList<String> getColorList(){
        String[] tmp = new String[CanvasColor.values().length];
        int count = 0;
        for (CanvasColor cv: CanvasColor.values()) {
            tmp[count] = cv.toString();
            count++;
        }
        return FXCollections.observableArrayList(tmp);
    }

    public static Color getColor(CanvasColor c) {
        return colorMap.get(c);
    }


}