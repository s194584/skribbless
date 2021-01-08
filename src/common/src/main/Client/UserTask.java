package common.src.main.Client;


import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.UnknownHostException;
import common.src.main.Enum.*;
import org.jspace.Space;

public class UserTask extends Task {
    protected String name;
    protected int userID;
    protected String roomName;
    protected String hostPort;
    protected int score;
    protected boolean isTurn;

    RemoteSpace serverSpace;
    protected Space chat;
    protected Space ui;

    public UserTask(Space ui){
        this.ui = ui;
    }

    @Override
    protected Integer call() throws Exception {
        if(isCancelled()){
            return 1;
        }
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        String uri = "";

        //get the ip for the server
        while (true) { //TODO: This must be done through UI
            System.out.print("Enter the server ip in the form host:gate");

            uri = ui.get(new ActualField(UiFlag.IP),new FormalField(String.class))[1].toString();
            hostPort = uri;

            // "tcp://localhost:9001/serverSpace?keep"
            uri = makeUri("serverSpace");
            System.out.println(uri);


            try {

                serverSpace = new RemoteSpace(uri);
                break;
            } catch (Exception e) {
                System.out.println("Can't find server");
            }
        }

        // request and get id from server
        serverSpace.put(InitialMessage.CONNECTED,"");
        userID = (int) serverSpace.get(new FormalField(Integer.class))[0];
        System.out.println("myID: " + userID);

        // Read username from UI
        name = (String) ui.get(new ActualField(UiFlag.NAME),new FormalField(String.class))[1];

                    // Choose to host a room or join one
            while (true) { //TODO: This must be done through UI
                System.out.print("type room-id or HOST:");
                try {
                    String tempRoom = (String) ui.get(new ActualField(UiFlag.ROOMNAME),new FormalField(String.class))[1];
                    if (!tempRoom.equals("HOST")) {
                        serverSpace.put(userID,tempRoom,InitialMessage.JOIN);
                    }
                    else {
                        serverSpace.put(userID,"",InitialMessage.HOST);
                    }

                    //get the response from the server
                    Object[] response = serverSpace.get(new ActualField(userID),new FormalField(InitialMessage.class),new FormalField(String.class));
                    if (response[1].equals(InitialMessage.OK)) {
                        roomName = response[2].toString();

                        // get ok from creationHandler
                        serverSpace.get(new ActualField(userID),new ActualField(InitialMessage.OK));
                        System.out.println(makeUri(roomName));
                        chat = new RemoteSpace(makeUri(roomName));
                        System.out.println("Client connected to: "+makeUri(roomName));
                        updateMessage("CONNECTED");
                        break;
                    } else {
                        System.out.println("Room not found");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        return 1;
    }

//    public static void main(String[] args) {
//
//        try {
//
//
//
//            // Read user name from the console
//            System.out.print("Enter your name: "); //TODO: This must be done through UI
//            name = input.readLine();
//
//            // Choose to host a room or join one
//            while (true) { //TODO: This must be done through UI
//                System.out.print("type room-id or HOST:");
//                String choice = input.readLine();
//                try {
//                    InitialMessage initialMessage;
//                    if (!choice.equals("HOST")) {
//                        initialMessage = InitialMessage.JOIN;
//                        serverSpace.put(userID,choice,initialMessage);
//                    }
//                    else {
//                        initialMessage = InitialMessage.HOST;
//                        serverSpace.put(userID,"",initialMessage);
//                    }
//
//
//                    //get the response from the server
//                    Object[] response = serverSpace.get(new ActualField(userID),new FormalField(InitialMessage.class),new FormalField(String.class));
//                    if (response[1].equals(InitialMessage.OK)) {
//                        roomName = response[2].toString();
//
//                        // get ok from creationHandler
//                        serverSpace.get(new ActualField(userID),new ActualField(InitialMessage.OK));
//                        System.out.println(makeUri(roomName));
//                        chat = new RemoteSpace(makeUri(roomName));
//                        break;
//                    } else {
//                        System.out.println("Room not found");
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//            // In the game:
//
//            //TODO: Start a new thread that handel incomming messages.
//
//            while (true) { //TODO: This must handel both puttting chat messages or drawing on the canvas.
//                String message = input.readLine();
//                chat.put(name, message);
//            }
//
//
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//    }

    private String makeUri(String identifier) {
        return "tcp://" + hostPort + "/" + identifier + "?keep";
    }

}
