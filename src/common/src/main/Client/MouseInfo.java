package common.src.main.Client;

import common.src.main.Enum.CanvasColor;
import common.src.main.Enum.CanvasTool;

import java.util.ArrayList;

public class MouseInfo {

    private ArrayList<double[]> lines = new ArrayList<>();
    private CanvasTool ct;
    private CanvasColor cc;

    public MouseInfo(CanvasTool ct, CanvasColor cc) {
        this.ct = ct;
        this.cc = cc;
    }

    public ArrayList<double[]> getLines() {
        return lines;
    }
    public void addLine(double x1, double y1, double x2, double y2){
        lines.add(new double[]{x1,y1,x2,y2});
    }

    public CanvasColor getCc() {
        return cc;
    }

    public CanvasTool getCt() {
        return ct;
    }
}
