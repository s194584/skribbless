package common.src.main.Server;

import common.src.main.Enum.ServerFlag;
import common.src.main.Enum.RoomMessage;
import org.jspace.*;

import java.util.HashMap;

public class Room implements Runnable {
    protected SpaceRepository repo;
    protected Space serverSpace;
    protected Space chat;

    protected String roomName;
    protected String currentWord; //The word which is being drawn
    protected static int playerAmount = 0;
    protected static HashMap<Integer,Space> playerInboxes;


    public Room(SpaceRepository repo,String roomName,Space serverSpace) {
        this.repo = repo;
        this.roomName = roomName;
        this.serverSpace = serverSpace;
    }

    @Override
    public void run() {
        try {
            // Create a local space for the chat messages
            SequentialSpace chat = new PileSpace();

            // Add the space to the repository
            repo.add(roomName, chat);
            serverSpace.put(roomName, ServerFlag.ROOMOK);
            System.out.println("Room added: " + roomName);

            // Waiting on game to start
            Template initialMessageTemplate = new Template (new FormalField(String.class),
                    new FormalField(RoomMessage.class));  //Get name,enum

            // We initially use chat to start game.
            while (true) {
                //Object[] message = chat.get(initialMessageTemplate) //TODO: why does this not work??
                Object[] message = chat.get(new FormalField(String.class), new FormalField(RoomMessage.class));
                if (message[1].equals(RoomMessage.NEWPLAYER)) {
                    addplayer(message[0].toString(),repo); //add player and generate their inbox
                } else if (message[1].equals(RoomMessage.STARTGAME)) {
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
        return name + "-" + playerAmount;
    }

    public Space getChat() {
        return chat;
    }
}

// A thread for sending messages to inboxes (Not sure if needed).
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