package common.src.main.DataTransfer;

import common.src.main.Enum.CanvasColor;
import common.src.main.Enum.CanvasTool;

import java.util.ArrayList;

/**
 *   Data Transfer object made to transfer the drawing through a tuple space.
 *   It does so using mouse positions and a color enum
 */


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
