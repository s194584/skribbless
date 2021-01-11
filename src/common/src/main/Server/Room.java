package common.src.main.Server;

import common.src.main.Enum.RoomFlag;
import common.src.main.Enum.ServerFlag;
import org.jspace.*;

import java.util.ArrayList;
import java.util.HashMap;

import static common.src.main.Enum.RoomFlag.CONNECTED;

public class Room implements Runnable {
    protected SpaceRepository repo;
    protected Space serverSpace;
    protected Space lobby;

    protected String roomName;
    protected String currentWord; //The word which is being drawn

    protected int playerAmount = 0;
    protected HashMap<Integer,Space> playerInboxes;

    //TODO: Some amount of characters to let players differentiate each other (in-case of the same name)

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
                    new FormalField(RoomFlag.class));  //Get name,enum

            while (true){
                Object[] message = lobby.get(new FormalField(RoomFlag.class),
                                                new FormalField(Integer.class),
                                                new FormalField(Object.class));
                switch ((RoomFlag) message[0]){
                    case CONNECTED:
                        System.out.println("User: "+message[1].toString()+" has connected");
                        break;
                }






            }



        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void addplayer(String name, SpaceRepository repo) {
        Space inbox = generateInbox(name, repo);
        playerInboxes.put(playerAmount,inbox);
        playerAmount++;
    }

    private Space generateInbox(String name, SpaceRepository repo) {
        Space inbox = new SequentialSpace();
        repo.add(createName(name),inbox);
        return inbox;
    }

    private String createName(String name) {
        return name + "-" + playerAmount;
    }

    public Space getLobby() {
        return lobby;
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