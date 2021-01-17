package common.src.main.Server;

import common.src.main.Client.TextInfo;
import common.src.main.Enum.RoomFlag;
import common.src.main.Enum.RoomResponseFlag;
import common.src.main.Enum.ServerFlag;
import common.src.main.Client.User;
import javafx.scene.paint.Color;
import org.jspace.*;

import java.util.*;

public class Room implements Runnable {
    protected SpaceRepository repo;
    protected Space serverSpace;
    protected Space lobby;

    protected String roomName;
    protected String currentWord = "notnull"; //The word which is being drawn
    protected int numberOfRounds;
    protected int turnTime;
    protected int timeLeft;
    protected int turnNumber = -1;

    protected int playerAmount = 0;
    protected int playerAmountGuessed = 0;
    protected ArrayList<User> users = new ArrayList<>();
    protected HashMap<Integer, Space> playerInboxes = new HashMap<>();
    protected HashMap<Integer, String> playerNames = new HashMap<>(); //This is used for messages in the chat.
    protected HashMap<Integer, Boolean> playerGuessed = new HashMap<>();

    // Timer
    Timer turnTimer;
    TimerTask takeTurnTime;
    private boolean gameStarted = false;

    //TODO: Some amount of characters to let players differentiate each other (in-case of the same name)

    // projection 7 (only created if branch == then is taken)
    public Room(SpaceRepository repo,String roomName,Space serverSpace) throws InterruptedException {
        this.repo = repo;
        this.roomName = roomName;
        this.serverSpace = serverSpace;
        lobby = new SequentialSpace();
        repo.add(roomName, lobby);
    }

    @Override
    public void run() {
        try {
            // projection 12
            serverSpace.put(roomName, ServerFlag.ROOMOK);
            System.out.println("Room added: " + roomName);

            // Create instance of a Timer for later use
            turnTimer = new Timer();

            while (true) {

                Object[] message = lobby.get(new FormalField(RoomFlag.class),
                        new FormalField(Integer.class),
                        new FormalField(Object.class));
                int playerID = (int) message[1];
                Object data = message[2];

                System.out.println(roomName + " got message: " + message[0]);
                switch ((RoomFlag) message[0]) {
                    case CONNECTED:
                        User user = ((User) data);
                        //Generate inboxSpace and sent connection string, back to user. Add user to list
                        boolean isLeader = playerAmount == 0;
                        addPlayer(playerID);
                        playerNames.put(playerID, user.getName());
                        playerGuessed.put(playerID,false);

                        // Provide inbox to connected player
                        lobby.put(playerID, createName(playerID), isLeader);

                        System.out.println("User: " + message[1].toString() + " has connected");

                        //Broadcast arrival of new player to other players:
//                        broadcastUsersToInbox(RoomResponseFlag.NEWPLAYER, playerInboxes.get(playerID));
//                        user.setLeader(isLeader);
//                        users.add(user); //Add user after to ensure no duplicates
//                        broadcastToInboxes(RoomResponseFlag.NEWPLAYER, data);

                        // Alternative
                        user.setLeader(isLeader);
                        users.add(user);

                        User[] userstmp = new User[users.size()];
                        broadcastToInboxes(RoomResponseFlag.NEWPLAYER,users.toArray(userstmp));

                        break;
                    case DISCONNECTED:
                        // Remove inbox from list and repo.
                        System.out.println("User disconnected");
                        removePlayer(playerID);
                        users.remove(data);

                        // Select new leader if leader left.
                        //TODO: Select new leader if leader left.
                        if(users.size()==0){
                            System.out.println("No more player. Closing...");
                            serverSpace.put(ServerFlag.SETROOM,roomName);
                            return;
                        }
                        // Broadcast that player left to other players.
                        broadcastToInboxes(RoomResponseFlag.PLAYERREMOVED, data);
                        break;
                    case CANVAS:
                        broadcastExcept(RoomResponseFlag.CANVAS, data, playerID);
                        break;
                    case MESSAGE:
                        boolean correct = filterMessage(data);
                        if (gameStarted&&(playerID!=users.get(turnNumber).getId())&&correct&&!playerGuessed.get(playerID)) {
                            // Calculate score for guesser and drawer
                            for (User u: users) {
                                if(u.getId()==playerID){
                                    u.addScore(timeLeft*10+(currentWord.length()*10));
                                    users.get(turnNumber).addScore(100+(currentWord.length()*10));
                                    broadcastToInboxes(RoomResponseFlag.ADDPOINTS, new User[]{u, users.get(turnNumber)});
                                }
                            }
                            playerGuessed.put(playerID,true);
                            playerAmountGuessed++;
                            TextInfo textInfoOthers = createTextToOthers(data, playerNames.get(playerID));
                            TextInfo textInfoSelf = createTextToSelf(data, playerNames.get(playerID));
                            broadcastExcept(RoomResponseFlag.MESSAGE, textInfoOthers, playerID);
                            broadcastToOne(RoomResponseFlag.MESSAGE, textInfoSelf, playerID);
                        } else if(!correct){
                            TextInfo textInfo = createText(data, playerNames.get(playerID));
                            broadcastToInboxes(RoomResponseFlag.MESSAGE, textInfo);
                        }

                        // Reset guesses when all have guessed the word
                        if(gameStarted&&playerAmountGuessed==playerAmount-1){
                            nextPlayer();
                        }
                        break;
                    case GAMESTART: // UI and sync
                        int gameOptions[] = (int[]) data;
                        numberOfRounds = gameOptions[0];
                        turnTime = gameOptions[1];

                        broadcastToInboxes(RoomResponseFlag.GAMESTART,gameOptions);

                        takeTurnTime = new takeTimeTask();

                        nextPlayer();
                        break;
                    case WORDCHOOSEN:
                        gameStarted = true;
                        currentWord = data.toString();
                        timeLeft = turnTime;
                        takeTurnTime = new takeTimeTask();
                        turnTimer.schedule(takeTurnTime,0,1000);
                        //TODO: Send startTurn tag with length of word. and begin turns in gamecontrollers.
                        broadcastToInboxes(RoomResponseFlag.STARTTURN, new int[]{currentWord.length(), users.get(turnNumber).getId()});
                        break;
                }


            }


        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void nextPlayer() {
        // Stop drawing for last player
        if(gameStarted)
            broadcastToOne(RoomResponseFlag.STOPDRAW,0, users.get(turnNumber).getId()); // UI only

        // Stopping timer
        takeTurnTime.cancel();
        System.out.println("Moving to next player...");

        // Reset guesses
        for (Integer k:playerGuessed.keySet()) {
            playerGuessed.put(k,false);

        }
        playerAmountGuessed = 0;
        currentWord = "";



        // Lets it go around in a circle
        if (users.size()-1 == turnNumber) {
            numberOfRounds--;
            turnNumber = -1;
            broadcastToInboxes(RoomResponseFlag.NEXTROUND,numberOfRounds); // UI only
        }

        // Break if number of rounds is reached
        if (numberOfRounds == 0) {
            rankUsers();
            User[] userstmp = new User[users.size()];
            broadcastToInboxes(RoomResponseFlag.ENDGAME,users.toArray(userstmp));
            return;
        }

        // Prompts the next player with word choice
        turnNumber++;
        String possibleWords[] = generateWords();
        broadcastToOne(RoomResponseFlag.CHOOSEWORD,possibleWords,(users.get(turnNumber)).getId());
    }

    private void rankUsers() {
        users.sort(Comparator.comparingInt(User::getScore).reversed());
    }

    private String[] generateWords() {
        return WordUtil.generateWords();
    }

    private void broadcastToOne(RoomResponseFlag flag, Object data, int playerID) {
        try {
            (playerInboxes.get(playerID)).put(flag, data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void broadcastExcept(RoomResponseFlag flag, Object data, int playerID) {
        try {
            for (Map.Entry<Integer, Space> set : playerInboxes.entrySet()) {
                if (set.getKey().equals(playerID)) continue;
                set.getValue().put(flag, data);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Creates the text with color coding to be broadcasted
    private TextInfo createTextToOthers(Object data, String playerName) {
        return new TextInfo(playerName + ": " + "Guessed the word!", (Color.GREEN).toString());
    }

    private TextInfo createTextToSelf(Object data, String playerName) {
        return new TextInfo("You Guessed the word!", (Color.GREEN).toString());
    }

    private TextInfo createText(Object data, String playerName) {
        return new TextInfo(playerName + ": " + data.toString(), (Color.BLACK).toString());
    }

    private boolean filterMessage(Object data) {
        if(data.toString().isEmpty()){
            return false;
        }
        return currentWord.equals(data.toString());
    }

    private void broadcastUsersToInbox(RoomResponseFlag flag, Space inbox) {
        try {
            for (User user : users) {
                inbox.put(flag, user);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void removePlayer(int playerID) {
        String inboxName = createName(playerID);
        repo.remove(inboxName);
        playerInboxes.remove(playerID);
        playerGuessed.remove(playerID);
        playerAmount--;
    }

    private void broadcastToInboxes(RoomResponseFlag flag, Object data) {
        try {
            for (Space inbox : playerInboxes.values()) {
                inbox.put(flag, data);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void addPlayer(int playerID) {
        Space inbox = generateInbox(playerID);
        playerInboxes.put(playerID, inbox);
        playerAmount++;
    }

    private Space generateInbox(int playerID) {
        Space inbox = new SequentialSpace();
        repo.add(createName(playerID), inbox);
        return inbox;
    }

    private String createName(int playerID) {
        return roomName + "-" + playerID;
    }

    public Space getLobby() {
        return lobby;
    }



    class takeTimeTask extends TimerTask {
        @Override
        public void run() {
            if(Room.this.timeLeft == 0){
                nextPlayer();
                this.cancel();
            }
            broadcastToInboxes(RoomResponseFlag.TIMETICK, Room.this.timeLeft);
            Room.this.timeLeft--;
        }
    }



}