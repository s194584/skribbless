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
                System.out.println("Client connected");
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

                // projection 6
                System.out.println("proj6");
                serverSpace.put(uToC, action, roomName);
                // projection 7
                System.out.println("proj7");
                String branch = (String) serverSpace.get(cToU, new FormalField(String.class))[1];
                System.out.println("Client branches: " + branch);
                if (branch.equals("then")) {
                    // projection 14
                    System.out.println("proj14");
                    response = serverSpace.get(cToU, new FormalField(ServerFlag.class), new FormalField(String.class));
                } else {
                    // projection 17
                    System.out.println("proj17");
                    branch = (String) serverSpace.get(cToU, new FormalField(String.class))[1];
                    if (branch.equals("then")) {
                        // projection 18
                        System.out.println("proj18");
                        response = serverSpace.get(cToU, new FormalField(ServerFlag.class), new FormalField(String.class));
                    } else {
                        // projection 19
                        System.out.println("proj19");
                        response = serverSpace.get(cToU, new FormalField(ServerFlag.class), new FormalField(String.class));
                    }
                }
                // projection 20
                System.out.println("proj20");
                if (response[1] == ServerFlag.OK) {
                    System.out.println("proj20then");
                    connectToRoom((String) response[2]);
                    connected();
                    break;
                } else {
                    System.out.println("proj20else");
                    notConnected();
                }

            } else {
                System.out.println("proj2else");
                // projection 4
                notConnected();
            }
        }
//        //get the ip for the server
//        while (true) {
//            System.out.print("Enter the server ip in the form host:gate");
//
//            hostPort = uri;
//
//            // "tcp://localhost:9001/serverSpace?keep"
//            uri = makeUri("serverSpace");
//            System.out.println(uri);
//
//
//            try {
//                serverSpace = new RemoteSpace(uri);
//                break;
//            } catch (Exception e) {
//                ui.getAll(new FormalField(UiFlag.class),new FormalField(Object.class));
//                System.out.println("Can't find server");
//            }
//        }
//        // UI information
//
//        // request and get id from server
//        serverSpace.put(ServerFlag.CONNECTED, "");
//        userID = (int) serverSpace.get(new FormalField(Integer.class))[0];
//        System.out.println("myID: " + userID);
//
//
//        // Choose to host a room or join one
//        while (true) {
//            try {
//                if (!tempRoom.equals("HOST")) {
//                    System.out.println("JOINING");
//                    serverSpace.put(userID, tempRoom, ServerFlag.JOIN);
//                } else {
//                    System.out.println("HOSTING");
//                    serverSpace.put(userID, "", ServerFlag.HOST);
//                }
//
//                //get the response from the server
//                Object[] response = serverSpace.get(new ActualField(userID), new FormalField(ServerFlag.class), new FormalField(String.class));
//                if (response[1].equals(ServerFlag.OK)) {
//                    roomName = response[2].toString();
//                    System.out.println(makeUri(roomName));
//                    lobby = new RemoteSpace(makeUri(roomName));
//                    System.out.println("Client connected to: " + makeUri(roomName));
//                    updateMessage("CONNECTED");
//                    break;
//                } else {
//                    System.out.println("Room not found");
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//
            return 1;

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

