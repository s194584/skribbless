package common.src.main.Server;

import common.src.main.DataTransfer.TextInfo;
import common.src.main.Enum.Broadcast;
import common.src.main.Enum.RoomFlag;
import common.src.main.Enum.RoomResponseFlag;
import common.src.main.Enum.ServerFlag;
import common.src.main.DataTransfer.User;
import javafx.scene.paint.Color;
import org.jspace.*;

import java.util.*;

/**
 * The main task of the Room thread is to handle all game logic. This includes:'
 * - Broadcasting data from one player to the others
 * - Filtering incoming messages for the correct word and assign points accordingly
 * - Keeping track of time
 * - Informing players of game changes such as starting the game, new turns and ending the game.
 */

public class Room implements Runnable {
    protected SpaceRepository repo;
    protected Space serverSpace;
    protected Space lobby;
    protected Space broadcast;
    protected Template receiveTemplate; // The template used for receiving messages

    // Room information
    protected String roomName;
    protected String currentWord = ""; //The word which is being drawn
    protected int numberOfRounds; //UI only
    protected int turnNumber = -1;
    protected int playerAmount = 0;
    protected int playerAmountGuessed = 0; // How many players guessed the word

    // Keeps track of when to end the game.
    protected int totalTurns;
    protected int amountOfTurns = 0;

    // Players' information
    protected ArrayList<User> users = new ArrayList<>();
    protected HashMap<Integer, Space> playerInboxes = new HashMap<>();
    protected HashMap<Integer, String> playerNames = new HashMap<>(); //This is used for messages in the chat.
    protected HashMap<Integer, Boolean> playerGuessed = new HashMap<>();

    // Timer
    protected int turnTime;
    protected int timeLeft;
    Timer turnTimer;
    TimerTask takeTurnTime;
    private boolean gameAFoot = false;

    // projection 7 (only created if branch == then is taken)
    public Room(SpaceRepository repo, String roomName, Space serverSpace) {
        this.repo = repo;
        this.roomName = roomName;
        this.serverSpace = serverSpace;
        lobby = new SequentialSpace();
        repo.add(roomName, lobby);
        receiveTemplate = new Template(
                new FormalField(RoomFlag.class),
                new FormalField(Integer.class),
                new FormalField(Object.class));
        broadcast = new SequentialSpace();
    }

    @Override
    public void run() {
        try {
            // projection 12
            serverSpace.put(roomName, ServerFlag.ROOMOK);
            System.out.println("Room added: " + roomName);

            // Create instance of a Timer for later use
            turnTimer = new Timer();

            // Creating broadcaster
            new Thread(new Broadcaster(broadcast)).start();

            while (true) {

                // Get input from users and handle it
                Object[] message = lobby.get(receiveTemplate.getFields());
                int playerID = (int) message[1];
                Object data = message[2];

                switch ((RoomFlag) message[0]) {
                    case CONNECTED:
                        connected(playerID, (User) data);

                        // Broadcast all users to users.
                        User[] userstmp = new User[users.size()];
                        broadcast.put(Broadcast.ALL,RoomResponseFlag.NEWPLAYER, users.toArray(userstmp),0);
                        break;
                    case DISCONNECTED:
                        // Remove inbox from list and repo.
                        System.out.println("User disconnected");
                        removePlayer(playerID, (User) data);

                        // Check if room-thread should close
                        if (closingCheck()) return;

                        // Broadcast that player left to other players.
                        broadcast.put(Broadcast.ALL,RoomResponseFlag.PLAYERREMOVED, data,0);
                        break;
                    case CANVAS:
                        broadcast.put(Broadcast.EXCEPT,RoomResponseFlag.CANVAS, data, playerID);
                        break;
                    case MESSAGE:
                        filterAndBroadcastMessage(playerID, data);

                        // Reset guesses when all have guessed the word
                        if (gameAFoot && playerAmountGuessed == playerAmount - 1) {
                            // Clearing ChosenWord
                            currentWord = "";
                            nextTurn();
                        }
                        break;
                    case GAMESTART: //Get important data and broadcast it
                        int gameOptions[] = (int[]) data;
                        numberOfRounds = gameOptions[0];
                        totalTurns = numberOfRounds * playerAmount;
                        turnTime = gameOptions[1];

                        // UI and sync
                        broadcast.put(Broadcast.ALL,RoomResponseFlag.GAMESTART, gameOptions,0);

                        takeTurnTime = new takeTimeTask();
                        nextTurn();
                        break;
                    case WORDCHOSEN:
                        // Set up different fields
                        gameAFoot = true;
                        currentWord = data.toString();
                        timeLeft = turnTime;
                        takeTurnTime = new takeTimeTask();
                        turnTimer.schedule(takeTurnTime, 0, 1000);

                        // Let others know the
                        broadcast.put(Broadcast.ALL,RoomResponseFlag.STARTTURN, new int[]{currentWord.length(), users.get(turnNumber).getId()},0);
                        break;
                    default:
                        // If an unknown flag is received nothing is done.
                        break;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // This method handles all necessary logic for choosing the next player and stopping the game if needed.
    private void nextTurn() throws InterruptedException {
        // Stop drawing for last player
        if (gameAFoot)
            broadcast.put(Broadcast.ONE,RoomResponseFlag.STOPDRAW, 0, users.get(turnNumber).getId()); // UI only

        // Stopping timer
        System.out.println("Moving to next player...");

        // Reset guesses
        for (Integer k : playerGuessed.keySet()) {
            playerGuessed.put(k, false);
        }
        playerAmountGuessed = 0;

        // Allows turns
        if (users.size() - 1 == turnNumber) {
            numberOfRounds--;
            turnNumber = -1;
            broadcast.put(Broadcast.ALL,RoomResponseFlag.NEXTROUND, numberOfRounds,0); // UI only
        }

        // End game if totalTurns is reached
        if (amountOfTurns == totalTurns) {
            gameAFoot = false;
            rankUsers();
            User[] userstmp = new User[users.size()];
            broadcast.put(Broadcast.ALL,RoomResponseFlag.ENDGAME, users.toArray(userstmp),0); // Update UI at clients
            return;
        }

        // Prompts the next player with word choice
        // And updates variables
        amountOfTurns++;
        turnNumber++;
        String possibleWords[] = generateWords();
        broadcast.put(Broadcast.ONE,RoomResponseFlag.CHOOSEWORD, possibleWords, (users.get(turnNumber)).getId());
    }

    private void filterAndBroadcastMessage(int playerID, Object data) throws InterruptedException {
        boolean correct = filterMessage(data);
        if (gameAFoot && (playerID != users.get(turnNumber).getId()) && correct && !playerGuessed.get(playerID)) {
            // Calculate score for guesser and drawer
            calculateScore(playerID);
            // Save the player's guess as correct
            playerGuessed.put(playerID, true);
            playerAmountGuessed++;
            // Text to others
            TextInfo textInfoOthers = createTextToOthers(data, playerNames.get(playerID));
            // Text to itself
            TextInfo textInfoSelf = createTextToSelf(data, playerNames.get(playerID));
            // Broadcasting the texts
            broadcast.put(Broadcast.EXCEPT,RoomResponseFlag.MESSAGE, textInfoOthers, playerID);
            broadcast.put(Broadcast.ONE,RoomResponseFlag.MESSAGE, textInfoSelf, playerID);
        } else if (!correct) {
            // Text to all
            TextInfo textInfo = createText(data, playerNames.get(playerID));
            broadcast.put(Broadcast.ALL,RoomResponseFlag.MESSAGE, textInfo,0);
        }
    }

    // Creates the text with color coding to be broadcasted
    private TextInfo createTextToOthers(Object data, String playerName) {
        return new TextInfo(playerName + ": " + "Guessed the word!", (Color.GREEN).toString());
    }

    // Creates text to your self if you guessed the word
    private TextInfo createTextToSelf(Object data, String playerName) {
        return new TextInfo("You Guessed the word!", (Color.GREEN).toString());
    }

    // normal text parsed through.
    private TextInfo createText(Object data, String playerName) {
        return new TextInfo(playerName + ": " + data.toString(), (Color.BLACK).toString());
    }

    // Returns true if player guessed current word.
    private boolean filterMessage(Object data) {
        if (data.toString().isEmpty()) {
            return false;
        }
        return currentWord.equals(data.toString());
    }

    // Calculate scores if guessed correctly
    private void calculateScore(int playerID) throws InterruptedException {
        for (User u : users) {
            if (u.getId() == playerID) {
                u.addScore(timeLeft * 10 + (currentWord.length() * 10));
                users.get(turnNumber).addScore(100 + (currentWord.length() * 10));
                broadcast.put(Broadcast.ALL,RoomResponseFlag.ADDPOINTS, new User[]{u, users.get(turnNumber)},0);
            }
        }
    }

    // Checks if room-thread should terminate
    private boolean closingCheck() throws InterruptedException {
        if (users.size() == 0) {
            System.out.println("No more players. Closing...");
            serverSpace.put(ServerFlag.SETROOM, roomName);
            // Cancel the timer task if it is set
            if (takeTurnTime != null) {
                takeTurnTime.cancel();
            }
            return true;
        }
        return false;
    }

    // Adds user to important places
    private void connected(int playerID, User data) throws InterruptedException {
        User user = data;
        // Generate inboxSpace and sent connection string, back to user.
        // Add user to lists
        boolean isLeader = playerAmount == 0;
        addPlayer(playerID, user);

        // Provide inbox to connected player
        lobby.put(playerID, createName(playerID), isLeader);

        System.out.println("User: " + playerID + " has connected");

        user.setLeader(isLeader);
        users.add(user);
    }

    private void rankUsers() {
        users.sort(Comparator.comparingInt(User::getScore).reversed());
    }

    private String[] generateWords() {
        return WordUtil.generateWords();
    }

    // Removes player from different lists/maps
    private void removePlayer(int playerID, User user) {
        String inboxName = createName(playerID);
        repo.remove(inboxName);

        if(users.indexOf(user)<=turnNumber){
            turnNumber--;
        }

        users.remove(user);
        playerInboxes.remove(playerID);
        playerGuessed.remove(playerID);
        playerAmount--;
    }

    // Adds player to different lists/maps
    private void addPlayer(int playerID, User user) {
        Space inbox = generateInbox(playerID);
        playerInboxes.put(playerID, inbox);
        playerNames.put(playerID, user.getName());
        playerGuessed.put(playerID, false);
        playerAmount++;
    }

    // Generate inbox tuple space and add it to the repo.
    private Space generateInbox(int playerID) {
        Space inbox = new SequentialSpace();
        repo.add(createName(playerID), inbox);
        return inbox;
    }

    private String createName(int playerID) {
        return roomName + "-" + playerID;
    }

    /**
     * This class extends TimerTask to use custom actions when executed by a Timer.
     */
    class takeTimeTask extends TimerTask {
        @Override
        public void run() {
            try {
                // ChosenWord has been cleared
                if (Room.this.currentWord.equals("")) {
                    this.cancel();
                    return;
                }
                // Time ran out
                if (Room.this.timeLeft == 0) {
                    // Clearing ChosenWord
                    // We use an empty string since the Users are prevented from sending empty strings,
                    // and thus the room should never receive empty Strings.
                    currentWord = "";

                    nextTurn();
                    this.cancel();
                    return;
                }
                // Send a time tick
                Room.this.timeLeft--;
                broadcast.put(Broadcast.ALL,RoomResponseFlag.TIMETICK, Room.this.timeLeft,0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * The Broadcaster will broadcast anything that comes into the tuple space broadcast,
     * as long as the tuple has the form:
     * Broadcast - RoomResponseFlag - Object - Integer
     */
    class Broadcaster implements Runnable {

        private HashMap<Integer, Space> playerInboxes = Room.this.playerInboxes;
        private Space broadcasts;

        public Broadcaster(Space broadcasts) {
            this.broadcasts = broadcasts;
        }

        @Override
        public void run() {
            while (true) {
                Object[] msg = new Object[0];
                try {
                    msg = broadcasts.get(new FormalField(Broadcast.class),new FormalField(RoomResponseFlag.class),
                            new FormalField(Object.class), new FormalField(Integer.class));
                } catch (InterruptedException e) {e.printStackTrace();}

                Broadcast type = (Broadcast) msg[0];
                RoomResponseFlag flag = (RoomResponseFlag) msg[1];
                Object data = msg[2];
                int id = (int) msg[3];

                switch (type){
                    case ALL: broadcastToAll(flag,data);break;
                    case ONE: broadcastToOne(flag,data,id);break;
                    case EXCEPT: broadcastExcept(flag,data,id);break;
                }
            }


        }
        // Broadcast to the inbox of the given playerID.
        private void broadcastToOne(RoomResponseFlag flag, Object data, int playerID) {
            try {
                (playerInboxes.get(playerID)).put(flag, data);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        // Broadcast to all inboxes except the one of the given playerID.
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

        // Broadcast to all inboxes.
        private void broadcastToAll(RoomResponseFlag flag, Object data) {
            try {
                for (Space inbox : playerInboxes.values()) {
                    inbox.put(flag, data);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}