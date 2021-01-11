package common.src.main.Server;

import common.src.main.Enum.RoomFlag;
import common.src.main.Enum.RoomResponseFlag;
import common.src.main.Enum.ServerFlag;
import common.src.main.Client.User;
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
    protected ArrayList<User> users = new ArrayList<User>();
    protected HashMap<Integer,Space> playerInboxes = new HashMap<>();

    //TODO: Some amount of characters to let players differentiate each other (in-case of the same name)

    public Room(SpaceRepository repo,String roomName,Space serverSpace) {
        this.repo = repo;
        this.roomName = roomName;
        this.serverSpace = serverSpace;
    }

    @Override
    public void run() {
        try {
            // Create a local space (lobby)
            lobby = new SequentialSpace();

            // Add the space to the repository
            repo.add(roomName, lobby);
            serverSpace.put(roomName, ServerFlag.ROOMOK);
            System.out.println("Room added: " + roomName);


            // Waiting on game to start
            Template initialMessageTemplate = new Template (new FormalField(String.class),
                    new FormalField(RoomFlag.class));  //Get name,enum

            while (true){
                Object[] message = lobby.get(new FormalField(RoomFlag.class),
                                                new FormalField(Integer.class),
                                                new FormalField(Object.class));
                int playerID = (int) message[1];
                Object data = message[2];

                switch ((RoomFlag) message[0]){
                    case CONNECTED:
                        //Generate inboxSpace and sent connection string, back to user. Add user to list
                        boolean isLeader = playerAmount == 0;
                        addplayer(playerID);
                        lobby.put(playerID,createName(playerID),isLeader);
                        users.add((User) data);
                        System.out.println("User: "+message[1].toString()+" has connected");

                        //Broadcast arrival of new player to other players:
                        broadcastToInboxes(RoomResponseFlag.NEWPLAYER,data);
                        break;
                    case DISCONNECTED:
                        // Remove inbox from list and repo.
                        removePlayer(playerID);
                        users.remove((User) data);

                        // Select new leader if leader left.
                        //TODO: Select new leader if leader left.

                        // Broadcast that player left to other players.
                        broadcastToInboxes(RoomResponseFlag.PLAYERREMOVED,data);
                        break;
                    case CANVAS:
                        break;
                    case MESSAGE:
                        break;
                }






            }



        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void removePlayer(int playerID) {
        String inboxName = createName(playerID);
        repo.remove(inboxName);
        playerInboxes.remove(playerID);
        playerAmount--;
    }

    private void broadcastToInboxes(RoomResponseFlag flag, Object data) {
        try {
            for(Space inbox : playerInboxes.values()) {
                inbox.put(flag,data);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void addplayer(int playerID) {
        Space inbox = generateInbox(playerID);
        playerInboxes.put(playerID,inbox);
        playerAmount++;
    }

    private Space generateInbox(int playerID) {
        Space inbox = new SequentialSpace();
        repo.add(createName(playerID),inbox);
        return inbox;
    }

    private String createName(int playerID) {
        return roomName + "-" + playerID;
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