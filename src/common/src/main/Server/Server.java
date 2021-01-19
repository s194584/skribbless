package common.src.main.Server;

import org.jspace.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import common.src.main.Enum.*;

/**
 * The main server instance responsible for connecting clients by assigning them their own creationHandler thread
 * and keeping track of currently active rooms.
 */

public class Server {

    protected static int id = 0; // playerIDs
    protected static int roomAmount = 0; // RoomIDs

    // HashMap of currently active rooms that players are allowed to join
    protected static HashMap<String, Boolean> rooms = new HashMap<>();

    public static void main(String[] args) {

        try {
            // Create a repository
            SpaceRepository repository = new SpaceRepository();

            // Create a local space all communication with the server happens through this space.
            SequentialSpace space = new SequentialSpace();

            // Add the space to the repository
            repository.add("serverSpace", space);

            // the hosting port, which is currently hard coded.
            // TODO: Mabey make this not hard coded
            String gateUri = "tcp://localhost:9001/?keep";
            System.out.println("Opening repository gate at " + gateUri + "...");
            repository.addGate(gateUri);

            // The eternal life of the server:
            while (true) {
                Object[] message = space.get(new FormalField(ServerFlag.class), new FormalField(String.class));
                System.out.println("server got message: " + message[0]);
                switch (ServerFlag.valueOf(message[0].toString())) {
                    case CONNECTED:
                        space.put(id);
                        openCreationHandler(id, space, repository);
                        id++;
                        break;
                    case CHECKROOM:
                        space.put(message[1], checkRoom(message[1].toString()));
                        break;
                    case GENERATEROOM:
                        space.put(ServerResponseFlag.GENERATEDROOM, createRoomName());
                        break;
                    case SETROOM:
                        setRoom(message[1].toString());
                        break;
                    default:
                        break;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    private static void openCreationHandler(int id, SequentialSpace space, SpaceRepository repository) {
        new Thread(new CreationHandler(id, space, repository)).start();
    }

    // Flip if room is currently active
    private static void setRoom(String roomName) {
        if (rooms.getOrDefault(roomName, false)) {
            rooms.remove(roomName);
        } else {
            rooms.put(roomName, true);
        }
    }

    private static boolean checkRoom(String roomName) {
        return rooms.getOrDefault(roomName, false);
    }

    private static String createRoomName() {
        roomAmount++;
        String roomName = "room" + roomAmount;
        rooms.put(roomName, false);
        return roomName;
    }

}

/**
 *   CreationHandler used to handle all initial player interaction in order not to block the server
 *   It is implemented using the protocol described in the report.
 */


class CreationHandler implements Runnable {
    protected Space space;
    protected int id;
    protected SpaceRepository repository;

    public CreationHandler(int id, SequentialSpace space, SpaceRepository repository) {
        System.out.println("CreationHandler, id: " + id);
        this.space = space;
        this.id = id;
        this.repository = repository;
    }

    @Override
    public void run() {
        Object[] message;
        ActualField uToC = new ActualField("user" + id + "creation");
        String cToU = "creation" + id + "user";
        System.out.println(cToU);

        try {
            // projection 7
            System.out.println("proj7");
            message = space.get(uToC, new FormalField(ServerFlag.class), new FormalField(String.class));
            // projection 8
            System.out.println("proj8");
            if (message[1] == ServerFlag.HOST) {
                space.put(cToU, "then");
                // projection 9
                System.out.println("proj9");
                space.put(ServerFlag.GENERATEROOM, "");
                // projection 10
                System.out.println("proj10");
                message = space.get(new ActualField(ServerResponseFlag.GENERATEDROOM), new FormalField(String.class));
                // projection 11
                System.out.println("proj11");
                createRoom((String) message[1]);
                // projection 12
                System.out.println("proj12");
                space.get(new ActualField(message[1]), new FormalField(ServerFlag.class));
                // projection 13
                System.out.println("proj13");
                space.put(ServerFlag.SETROOM, message[1]);
                // projection 15
                System.out.println("proj15");
                space.put(cToU, true, message[1]);
            } else {
                space.put(cToU, "else");
                // projection 16
                System.out.println("proj16");
                space.put(ServerFlag.CHECKROOM, message[2]);
                // projection 17
                System.out.println("proj17");
                Object[] serverResponse = space.get(new ActualField(message[2]), new FormalField(Boolean.class));
                // projection 18
                space.put(cToU, serverResponse[1], serverResponse[0]);
                System.out.println("proj18");
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void createRoom(String roomName) throws InterruptedException {
        new Thread(new Room(repository, roomName, space)).start();
        System.out.println("creatingRoom: " + roomName);


    }

}
