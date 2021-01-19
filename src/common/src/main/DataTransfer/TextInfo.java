package common.src.main.DataTransfer;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 *   Data transfer object for messages between clients.
 *   Made so color coding can also be parsed
 */

public class TextInfo {
   private String content;
   private String color;

    public TextInfo(String content, String color) {
        this.content = content;
        this.color = color;
    }

    public String getContent() {
        return content;
    }

    public String getColor() {
        return color;
    }

    public Text getText() {
        Text text = new Text(content);
        text.setFill(Color.valueOf(color));
        return text;
    }
}
