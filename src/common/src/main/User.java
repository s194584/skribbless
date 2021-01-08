package common.src.main;

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
    protected int id;
    protected int score;
    protected boolean isturn;

    protected static Space chat;

    public static void main(String[] args) {

        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

            String uri = "";
            RemoteSpace serverSpace;

            //get the ip for the server
            while (true) { //TODO: This must be done through UI
                System.out.print("Enter the server ip in the form host:gate");
                uri = input.readLine();
                uri = "tcp://" + uri + "/serverSpace?keep";
                uri = "tcp://localhost:9001/serverSpace?keep"; //TODO: remove this
                try {

                    serverSpace = new RemoteSpace(uri);
                    break;
                } catch (Exception e) {
                    System.out.println("Can't find server");
                }
            }

            // Read user name from the console
            System.out.print("Enter your name: "); //TODO: This must be done through UI
            name = input.readLine();

            // Choose to host a room or join one
            while (true) { //TODO: This must be done through UI
                System.out.print("type room-id or HOST:");
                String choice = input.readLine();
                try {
                    InitialMessage initialMessage;
                    if (choice.equals("HOST")) {
                        initialMessage = InitialMessage.JOIN;
                        serverSpace.put(choice,initialMessage);
                    }
                    else {
                        initialMessage = InitialMessage.HOST;
                        serverSpace.put(name,initialMessage);
                    }


                    //get the response from the server
                    Object[] response = serverSpace.get(new FormalField(InitialMessage.class));
                    if (response[0].equals(InitialMessage.OK)) {
                        Object[] Objectchat = serverSpace.get(new FormalField(Space.class));
                        chat = (Space) Objectchat[0];
                    } else {

                    }
                    break;
                } catch (Exception e) {
                    System.out.println("Not a recognized command");
                }
            }





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

}
