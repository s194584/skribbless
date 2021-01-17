package common.src.main.Client;


import common.src.main.Server.Server;
import javafx.concurrent.Task;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import common.src.main.Enum.*;
import org.jspace.Space;

public class UserTask extends Task {
    protected String name;  // Needed
    protected int id; // Needed

    protected String roomName;
    protected String hostPort; //Needed
//    protected int score;
//    protected boolean isTurn;

    RemoteSpace serverSpace;
    protected Space lobby; // Needed
    protected Space ui;
    private boolean connection;

    public UserTask(Space ui) {
        this.ui = ui;

    }

    public TaskInfo getTaskInfo() {
        return new TaskInfo(name, id, lobby,hostPort);
    }

    @Override
    protected Integer call() throws Exception {
        while (true) {
            if (isCancelled()) {
                return 1;
            }

            // Information from ui
            hostPort = ui.get(new ActualField(UiFlag.IP), new FormalField(String.class))[1].toString();
            name = (String) ui.get(new ActualField(UiFlag.NAME), new FormalField(String.class))[1];
            String roomName = (String) ui.get(new ActualField(UiFlag.ROOMNAME), new FormalField(String.class))[1];
            ServerFlag action = (ServerFlag) ui.get(new ActualField(UiFlag.ACTION), new FormalField(ServerFlag.class))[1];

            // Helper
            Object[] response;

            // projection 1
            System.out.println("proj1");
            connect();
            // projection 2
            if (isConnected()) {
                System.out.println("proj2then");
                // projection 3
                System.out.println("proj3");
                serverSpace.put(ServerFlag.CONNECTED, "");
                // projection 4
                System.out.println("proj4");
                id = (int) serverSpace.get(new FormalField(Integer.class))[0];
                System.out.println("Client got id: " + id);

                // Shorthand
                ActualField cToU = new ActualField("creation" + id + "user");
                String uToC = "user" + id + "creation";

                // projection 7
                System.out.println("proj7");
                serverSpace.put(uToC, action, roomName);
                // projection 8
                System.out.println("proj8");
                String branch = (String) serverSpace.get(cToU, new FormalField(String.class))[1];
                System.out.println("Client branches: " + branch);
                if (branch.equals("then")) {
                    // projection 15
                    System.out.println("proj15");
                    response = serverSpace.get(cToU,new FormalField(Boolean.class),new FormalField(String.class));
                } else {
                    // projection 18
                    System.out.println("proj18");
                    response = serverSpace.get(cToU,new FormalField(Boolean.class),new FormalField(String.class));
                }
                // projection 19
                System.out.println("proj19");
                if ((Boolean) response[1]) {
                    System.out.println("proj19then");
                    // projection 20
                    System.out.println("proj20");
                    connectToRoom((String) response[2]);
                    // projection 21
                    System.out.println("proj21");
                    connected();
                } else {
                    System.out.println("proj19else");
                    // projection 22
                    System.out.println("proj22");
                    notConnected();
                }

            } else {
                System.out.println("proj2else");
                // projection 23
                System.out.println("proj23");
                notConnected();
            }
        }
    }

    private void notConnected() throws InterruptedException {
        updateMessage("NOTCONNECTED");
        ui.getAll(new FormalField(UiFlag.class),new FormalField(Object.class));
    }

    private void connected() {
        updateMessage("CONNECTED");
    }

    private void connectToRoom(String o) throws IOException {
        lobby = new RemoteSpace(makeUri(o));
    }

    private boolean isConnected() {
        return connection;
    }

    private void connect() {
        try {
            serverSpace = new RemoteSpace(makeUri("serverSpace"));
            connection = true;
        } catch (Exception e) {
            connection = false;
            System.out.println("Can't find server");
        }
    }

    private String makeUri(String identifier) {
        return "tcp://" + hostPort + "/" + identifier + "?keep";
    }

}

