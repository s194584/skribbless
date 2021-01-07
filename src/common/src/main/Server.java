package common.src.main;

import org.jspace.*;

public class Server {

    protected String currentWord; //The word which is being drawn


    public static void main(String[] args) {

        try {
            // Create a repository
            SpaceRepository repository = new SpaceRepository();

            // Create a local space for the chat messages
            SequentialSpace chat = new PileSpace();

            // Add the space to the repository
            repository.add("chat", chat);

            String gateUri = "tcp://localhost:9001/?keep";
            System.out.println("Opening repository gate at " + gateUri + "...");
            repository.addGate(gateUri);

            while (true) {
                Object[] t = chat.get(new FormalField(String.class), new FormalField(String.class)); //Get messages
                System.out.println(t[0] + ": " + t[1]);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}

class sendMesseges implements Runnable {
    Space userInbox;

    sendMesseges(Space space) {
        this.userInbox = space;
    }

    @Override
    public void run() {
        //TODO: send messages to inboxes of every user in the room
    }
}