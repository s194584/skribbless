package common.src.main;

import org.jspace.RemoteSpace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;

public class User {
    protected String name;
    protected int id;
    protected int score;
    protected boolean isturn;

    public static void main(String[] args) {

        try {
            String uri = "tcp://localhost:9001/chat?keep";

            RemoteSpace chat = new RemoteSpace(uri);

            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

            // Read user name from the console
            System.out.print("Enter your name: ");
            String name = input.readLine();

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
