package common.src.main.Client;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;

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
