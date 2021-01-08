package common.src.main.Server;

import org.jspace.*;

import java.util.HashMap;
import common.src.main.Enum.*;

public class Server {

    protected static int id = 0;
    protected static int roomAmount = 0;
    protected static HashMap<String,Boolean> rooms = new HashMap<String,Boolean>();

    public static void main(String[] args) {

        try {
            // Create a repository
            SpaceRepository repository = new SpaceRepository();

            // Create a local space for the chat messages
            SequentialSpace serverSpace = new SequentialSpace();

            // Add the space to the repository
            repository.add("serverSpace", serverSpace);

            String gateUri = "tcp://localhost:9001/?keep";
            System.out.println("Opening repository gate at " + gateUri + "...");
            repository.addGate(gateUri);

            // Wait on people to connect
            while (true) {
                Object[] message = serverSpace.get(new FormalField(InitialMessage.class),new FormalField(String.class));
                System.out.println("server got message: " + message[0]);
                switch (InitialMessage.valueOf(message[0].toString())) {
                    case CONNECTED:
                        serverSpace.put(id);
                        new Thread(new CreationHandler(serverSpace,id,repository,createRoomName())).start();
                        id++;
                        break;
                    case CHECKROOM:
                        boolean roomExists = checkRoom(message[1].toString());
                        serverSpace.put(message[1],roomExists);
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

    private static void setRoom(String roomName) {
        rooms.put(roomName,true);
    }

    private static boolean checkRoom(String roomName) {
        return rooms.get(roomName);
    }

    private static String createRoomName() {
        roomAmount++;
        String roomName = "room" + "-" + roomAmount;
        rooms.put(roomName,false);
        return roomName;
    }

}

class CreationHandler implements Runnable {
    protected Space space;
    protected int playerID;
    protected SpaceRepository repository;
    protected String roomName;

    public CreationHandler(SequentialSpace serverSpace, int id,SpaceRepository repository, String roomName) {
        System.out.println("CreationHandler, id: " + id + " roomName: " + roomName);
        this.space = serverSpace;
        this.playerID = id;
        this.repository = repository;
        this.roomName = roomName;
    }

    @Override
    public void run() {
        Template initialMessageTemplate = new Template (new ActualField(playerID),new FormalField(String.class),
                new FormalField(InitialMessage.class));  //Get name,enum

        try {
            while (true) {
                Object[] message = space.get(initialMessageTemplate.getFields());
                if (message[2].equals(InitialMessage.HOST)) {
                    Room room = new Room(repository,roomName,space);
                    new Thread(room).start();
                    System.out.println("creatingRoom: " + roomName);

                    // send gate string to user
                    space.put(playerID,InitialMessage.OK,roomName);
                    //set roomName to true
                    space.put(InitialMessage.SETROOM,roomName);

                    // get ok from room
                    space.get(new ActualField(roomName),new ActualField(InitialMessage.ROOMOK));
                    space.put(playerID,InitialMessage.OK);

                    break;
                } else { // Must be JOIN
                    roomName = message[1].toString(); //update roomName to requested one
                    space.put(InitialMessage.CHECKROOM,roomName);
                    Object[] serverResponse = space.get(new ActualField(roomName),new FormalField(Boolean.class));
                    System.out.println("room exists: " + serverResponse[1]);
                    if ((boolean) serverResponse[1]) {
                        space.put(playerID,InitialMessage.OK,roomName);
                        System.out.println("joining room: " + roomName);
                        break;
                    }
                    // Room does not exist
                    space.put(playerID,InitialMessage.KO,"");
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
