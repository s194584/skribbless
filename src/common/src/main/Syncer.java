package common.src.main;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;
import org.jspace.FormalField;
import org.jspace.Space;

public class Syncer implements Runnable{
    SimpleStringProperty l;
    Space s;
    public Syncer(SimpleStringProperty l, Space s){
        this.l = l;
        this.s = s;
    }

    @Override
    public void run() {
        try {

            System.out.println("Entered Run...");
            System.out.println("TRYING TO UPDATE LABEL...");
            l.setValue(s.get(new FormalField(String.class))[0].toString());
            System.out.println("Updated label");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

