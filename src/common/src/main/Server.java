package common.src.main;

import org.jspace.*;

import java.util.HashMap;
import common.src.main.Enum.initialMessages;

public class Server {

    protected String currentWord; //The word which is being drawn
    protected static int playerAmount = 0;
    protected static HashMap<Integer,Space> playerInboxes;
    protected SpaceRepository repo;

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

            //Waiting on game to start
            Template initialMessageTemplate = new Template (new FormalField(String.class),
                                                            new FormalField(initialMessages.class));  //Get name,enum

            while (true) {
                //Object[] message = chat.get(initialMessageTemplate) //TODO: why does this not work??
                Object[] message = chat.get(new FormalField(String.class), new FormalField(initialMessages.class));
                if (message[1].equals(initialMessages.NEWPLAYER)) {
                    addplayer(message[0].toString(),repository); //add player and generate their inbox
                } else if (message[1].equals(initialMessages.STARTGAME)) {
                    break;
                }
            }

            //Chat function
            while (true) {
                Object[] t = chat.get(new FormalField(String.class), new FormalField(String.class)); //Get messages
                System.out.println(t[0] + ": " + t[1]);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    private static void addplayer(String name, SpaceRepository repo) {
        Space inbox = generateInbox(name, repo);
        playerInboxes.put(playerAmount,inbox);
        playerAmount++;
    }

    private static Space generateInbox(String name, SpaceRepository repo) {
        Space inbox = new SequentialSpace();
        repo.add(createName(name),inbox);
        return inbox;
    }

    private static String createName(String name) {
        return name + "-" + "playerAmount";
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