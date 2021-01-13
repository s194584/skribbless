package common.src.main.Client;

import common.src.main.Enum.CanvasColor;
import common.src.main.Enum.CanvasTool;
import javafx.scene.paint.Paint;

import java.util.HashMap;

public class MouseInfo {

    private double x1,y1,x2,y2;
    private CanvasTool ct;
    private CanvasColor cc;

    public MouseInfo(double x1, double y1, double x2, double y2, CanvasTool ct, CanvasColor cc) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.ct = ct;
        this.cc = cc;
    }

    public double getX1() {
        return x1;
    }

    public double getY1() {
        return y1;
    }

    public double getX2() {
        return x2;
    }

    public double getY2() {
        return y2;
    }

    public CanvasColor getCc() {
        return cc;
    }

    public CanvasTool getCt() {
        return ct;
    }
}
