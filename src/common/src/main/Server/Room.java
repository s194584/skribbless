package common.src.main.Server;

import common.src.main.Client.TextInfo;
import common.src.main.Enum.RoomFlag;
import common.src.main.Enum.RoomResponseFlag;
import common.src.main.Enum.ServerFlag;
import common.src.main.Client.User;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import org.jspace.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Room implements Runnable {
    protected SpaceRepository repo;
    protected Space serverSpace;
    protected Space lobby;

    protected String roomName;
    protected String currentWord = "justsoitsnotnull"; //The word which is being drawn

    protected int playerAmount = 0;
    protected ArrayList<User> users = new ArrayList<User>();
    protected HashMap<Integer,Space> playerInboxes = new HashMap<>();
    protected HashMap<Integer,String> playerNames = new HashMap<>(); //This is used for messages in the chat.

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

                System.out.println(roomName + " got message: " + message[0]);

                switch ((RoomFlag) message[0]){
                    case CONNECTED:
                        //Generate inboxSpace and sent connection string, back to user. Add user to list
                        boolean isLeader = playerAmount == 0;
                        addplayer(playerID);
                        playerNames.put(playerID,((User) data).getName());
                        lobby.put(playerID,createName(playerID),isLeader);
                        System.out.println("User: "+message[1].toString()+" has connected");

                        //Broadcast arrival of new player to other players:
                        broadcastUsersToInbox(RoomResponseFlag.NEWPLAYER,playerInboxes.get(playerID));
                        users.add((User) data); //Add user after to ensure no duplicates
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
                        boolean correct = filterMessage(data);
                        TextInfo textInfo = createText(data,correct,playerNames.get(playerID));
                        broadcastToInboxes(RoomResponseFlag.MESSAGE,textInfo);
                        break;
                }






            }



        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Creates the text with color coding to be broadcasted
    private TextInfo createText(Object data, boolean correct, String playerName) {
        String content = playerName + ": " + data.toString();
        TextInfo textInfo;
        if (correct) {
            textInfo = new TextInfo(content, (Color.GREEN).toString());
        }
        textInfo = new TextInfo(content, (Color.BLACK).toString());
        return textInfo;
    }

    private boolean filterMessage(Object data) {
        return currentWord.equals(data.toString());
    }

    private void broadcastUsersToInbox(RoomResponseFlag flag, Space inbox) {
        try {
            for(User user : users) {
                inbox.put(flag,user);
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