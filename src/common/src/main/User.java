package common.src.main;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import common.src.main.Enum.*;
import org.jspace.Space;

public class User {
    protected static String name;
    protected static int userID;
    protected static String roomName;
    protected static String hostPort;
    protected int score;
    protected boolean isturn;

    protected static Space chat;
    protected static Space ui;

    public static void main(String[] args) {

        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

            String uri = "";
            RemoteSpace serverSpace;

            //get the ip for the server
            while (true) { //TODO: This must be done through UI
                System.out.print("Enter the server ip in the form host:gate");

                uri = input.readLine();
                hostPort = uri;
                uri = makeUri("serverSpace");
                uri = "tcp://" + uri + "/serverSpace?keep";
                uri = "tcp://localhost:9001/serverSpace?keep"; //TODO: remove this
                hostPort = "localhost:9001";

                try {

                    serverSpace = new RemoteSpace(uri);
                    break;
                } catch (Exception e) {
                    System.out.println("Can't find server");
                }
            }

            // request and get id from server
            serverSpace.put(ServerFlag.CONNECTED,"");
            userID = (int) serverSpace.get(new FormalField(Integer.class))[0];
            System.out.println("myID: " + userID);


            // Read user name from the console
            System.out.print("Enter your name: "); //TODO: This must be done through UI
            name = input.readLine();

            // Choose to host a room or join one
            while (true) { //TODO: This must be done through UI
                System.out.print("type room-id or HOST:");
                String choice = input.readLine();
                try {
                    ServerFlag initialMessage;
                    if (!choice.equals("HOST")) {
                            initialMessage = ServerFlag.JOIN;
                            serverSpace.put(userID,choice,initialMessage);
                        }
                        else {
                            initialMessage = ServerFlag.HOST;
                            serverSpace.put(userID,"",initialMessage);
                    }

                    //get the response from the server
                    Object[] response = serverSpace.get(new ActualField(userID),new FormalField(ServerFlag.class),new FormalField(String.class));
                    if (response[1].equals(ServerFlag.OK)) {
                        roomName = response[2].toString();

                        // get ok from creationHandler
                        serverSpace.get(new ActualField(userID),new ActualField(ServerFlag.OK));
                        System.out.println(makeUri(roomName));
                        chat = new RemoteSpace(makeUri(roomName));
                        break;
                    } else {
                        System.out.println("Room not found");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // In the game:

            //TODO: Start a new thread that handel incomming messages.

            while (true) { //TODO: This must handel both puttting chat messages or drawing on the canvas.
                String message = input.readLine();
                chat.put(name, message);
            }


        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private static String makeUri(String identifier) {
        return "tcp://" + hostPort + "/" + identifier + "?keep";
    }

}
