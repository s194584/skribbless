package common.src.main.Server;

import common.src.main.DataTransfer.TextInfo;
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

    protected String roomName;
    protected String currentWord = ""; //The word which is being drawn
    protected int numberOfRounds; //UI only
    protected int turnNumber = -1;
    // Keeps track of when to end the game.
    protected int totalTurns;
    protected int amountOfTurns = 0;

    protected int playerAmount = 0;
    protected int playerAmountGuessed = 0; // How many players guessed the word
    protected ArrayList<User> users = new ArrayList<>();
    protected HashMap<Integer, Space> playerInboxes = new HashMap<>();
    protected HashMap<Integer, String> playerNames = new HashMap<>(); //This is used for messages in the chat.
    protected HashMap<Integer, Boolean> playerGuessed = new HashMap<>();
    protected Template receiveTemplate; // The template used for receiving messages

    // Timer
    protected int turnTime;
    protected int timeLeft;
    Timer turnTimer;
    TimerTask takeTurnTime;
    private boolean gameAFoot = false;

    //TODO: Some amount of characters to let players differentiate each other (in-case of the same name)

    // projection 7 (only created if branch == then is taken)
    public Room(SpaceRepository repo, String roomName, Space serverSpace) throws InterruptedException {
        this.repo = repo;
        this.roomName = roomName;
        this.serverSpace = serverSpace;
        lobby = new SequentialSpace();
        repo.add(roomName, lobby);
        receiveTemplate = new Template(
                new FormalField(RoomFlag.class),
                new FormalField(Integer.class),
                new FormalField(Object.class));
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

                // Get input from users and handle it
                Object[] message = lobby.get(receiveTemplate.getFields());
                int playerID = (int) message[1];
                Object data = message[2];

                System.out.println(roomName + " got message: " + message[0]);
                switch ((RoomFlag) message[0]) {
                    case CONNECTED:
                        connected(playerID, (User) data);

                        // Broadcast all users to users.
                        User[] userstmp = new User[users.size()];
                        broadcastToInboxes(RoomResponseFlag.NEWPLAYER, users.toArray(userstmp));
                        break;

                    case DISCONNECTED:
                        // Remove inbox from list and repo.
                        System.out.println("User disconnected");
                        removePlayer(playerID, (User) data);

                        //TODO: Select new leader if leader left.

                        // Check if room-thread should close
                        if (closingCheck()) return;

                        // Broadcast that player left to other players.
                        broadcastToInboxes(RoomResponseFlag.PLAYERREMOVED, data);
                        break;
                    case CANVAS:
                        broadcastExcept(RoomResponseFlag.CANVAS, data, playerID);
                        break;
                    case MESSAGE:
                        filterAndBroadcastMessage(playerID, data);

                        // Reset guesses when all have guessed the word
                        if (gameAFoot && playerAmountGuessed == playerAmount - 1) {
                            nextTurn();
                        }
                        break;
                    case GAMESTART: //Get important data and broadcast it
                        int gameOptions[] = (int[]) data;
                        numberOfRounds = gameOptions[0];
                        totalTurns = numberOfRounds * playerAmount;
                        turnTime = gameOptions[1];

                        // UI and sync
                        broadcastToInboxes(RoomResponseFlag.GAMESTART, gameOptions);

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
                        broadcastToInboxes(RoomResponseFlag.STARTTURN, new int[]{currentWord.length(), users.get(turnNumber).getId()});
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
    private void nextTurn() {
        // Stop drawing for last player
        if (gameAFoot)
            broadcastToOne(RoomResponseFlag.STOPDRAW, 0, users.get(turnNumber).getId()); // UI only

        // Stopping timer
        System.out.println("Moving to next player...");

        // Reset guesses
        for (Integer k : playerGuessed.keySet()) {
            playerGuessed.put(k, false);

        }
        playerAmountGuessed = 0;

        // Clearing ChosenWord
        // We use an empty string since the Users are prevented from sending empty strings,
        // and thus the room should never receive empty words
        currentWord = "";

        // Allows turns
        if (users.size() - 1 == turnNumber) {
            numberOfRounds--;
            turnNumber = -1;
            broadcastToInboxes(RoomResponseFlag.NEXTROUND, numberOfRounds); // UI only
        }

        // End game if totalTurns is reached
        if (amountOfTurns == totalTurns) {
            gameAFoot = false;
            rankUsers();
            User[] userstmp = new User[users.size()];
            broadcastToInboxes(RoomResponseFlag.ENDGAME, users.toArray(userstmp)); // Update UI at clients
            return;
        }


        // Prompts the next player with word choice
        // And updates variables
        amountOfTurns++;
        turnNumber++;
        String possibleWords[] = generateWords();
        broadcastToOne(RoomResponseFlag.CHOOSEWORD, possibleWords, (users.get(turnNumber)).getId());
    }

    private void filterAndBroadcastMessage(int playerID, Object data) {
        boolean correct = filterMessage(data);
        if (gameAFoot && (playerID != users.get(turnNumber).getId()) && correct && !playerGuessed.get(playerID)) {
            // Calculate score for guesser and drawer
            calculateScore(playerID);

            playerGuessed.put(playerID, true);
            playerAmountGuessed++;
            TextInfo textInfoOthers = createTextToOthers(data, playerNames.get(playerID));
            TextInfo textInfoSelf = createTextToSelf(data, playerNames.get(playerID));
            broadcastExcept(RoomResponseFlag.MESSAGE, textInfoOthers, playerID);
            broadcastToOne(RoomResponseFlag.MESSAGE, textInfoSelf, playerID);
        } else if (!correct) {
            TextInfo textInfo = createText(data, playerNames.get(playerID));
            broadcastToInboxes(RoomResponseFlag.MESSAGE, textInfo);
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
    private void calculateScore(int playerID) {
        for (User u : users) {
            if (u.getId() == playerID) {
                u.addScore(timeLeft * 10 + (currentWord.length() * 10));
                users.get(turnNumber).addScore(100 + (currentWord.length() * 10));
                broadcastToInboxes(RoomResponseFlag.ADDPOINTS, new User[]{u, users.get(turnNumber)});
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
    private void broadcastToInboxes(RoomResponseFlag flag, Object data) {
        try {
            for (Space inbox : playerInboxes.values()) {
                inbox.put(flag, data);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Removes player from different lists/maps
    private void removePlayer(int playerID, User user) {
        String inboxName = createName(playerID);
        repo.remove(inboxName);
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

    public Space getLobby() {
        return lobby;
    }

    // The timer thread
    class takeTimeTask extends TimerTask {
        @Override
        public void run() {
            if (Room.this.currentWord.equals("")) {
                this.cancel();
                return;
            }
            if (Room.this.timeLeft == 0) {
                nextTurn();
                this.cancel();
                return;
            }
            broadcastToInboxes(RoomResponseFlag.TIMETICK, Room.this.timeLeft);
            Room.this.timeLeft--;
        }
    }


}