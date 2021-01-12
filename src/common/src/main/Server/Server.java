package common.src.main.Server;

import org.jspace.*;

import java.util.ArrayList;
import java.util.HashMap;
import common.src.main.Enum.*;

public class Server {

    protected static int id = 0;
    protected static int roomAmount = 0;
    protected static HashMap<String,Boolean> rooms = new HashMap<>();
    protected static ArrayList<String> unusedRooms = new ArrayList<>();

    public static void main(String[] args) {

        try {
            // Create a repository
            SpaceRepository repository = new SpaceRepository();

            // Create a local space for the chat messages
            SequentialSpace space = new SequentialSpace();

            // Add the space to the repository
            repository.add("serverSpace", space);

            String gateUri = "tcp://localhost:9001/?keep";
            System.out.println("Opening repository gate at " + gateUri + "...");
            repository.addGate(gateUri);

            // Wait on people to connect
            while (true) {
                Object[] message = space.get(new FormalField(ServerFlag.class),new FormalField(String.class));
                System.out.println("server got message: " + message[0]);
                switch (ServerFlag.valueOf(message[0].toString())) {
                    case CONNECTED:
                        space.put(id);
                        openCreationHandler(id,space,repository);
                        id++;
                        break;
                    case CHECKROOM:
                        space.put(message[1],checkRoom(message[1].toString()));
                        break;
                    case GENERATEROOM:
                        space.put(ServerResponseFlag.GENERATEDROOM,createRoomName());
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
        new Thread(new CreationHandler(id,space,repository)).start();
    }

    private static void setRoom(String roomName) {
        rooms.put(roomName,true);
    }

    private static boolean checkRoom(String roomName) {
        return rooms.getOrDefault(roomName,false);
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
    protected int id;
    protected SpaceRepository repository;
    protected String roomName;

    public CreationHandler(int id, SequentialSpace space, SpaceRepository repository) {
        System.out.println("CreationHandler, id: " + id);
        this.space = space;
        this.id = id;
        this.repository = repository;
    }

    @Override
    public void run() {
        Object[] message;
        ActualField uToC = new ActualField("user"+id+"creation");
        String cToU = "creation"+id+"user";
        System.out.println(cToU);

        try {
            // projection 6
            System.out.println("proj6");
            message = space.get(uToC, new FormalField(ServerFlag.class), new FormalField(String.class));
            // projection 7
            System.out.println("proj7");
            if (message[1] == ServerFlag.HOST) {
                space.put(cToU, "then");
                // projection 8
                System.out.println("proj8");
                space.put(ServerFlag.GENERATEROOM, "");
                // projection 9
                System.out.println("proj9");
                message = space.get(new ActualField(ServerResponseFlag.GENERATEDROOM), new FormalField(String.class));
                // projection 10
                System.out.println("proj10");
                createRoom((String) message[1]);
                // projection 11
                System.out.println("proj11");
                space.get(new ActualField(message[1]), new FormalField(ServerFlag.class));
                // projection 12
                System.out.println("proj12");
                space.put(ServerFlag.SETROOM, message[1]);
                // projection 14
                System.out.println("proj14");
                space.put(cToU, ServerFlag.OK, message[1]);
            } else {
                space.put(cToU, "else");
                // projection 15
                System.out.println("proj15");
                space.put(ServerFlag.CHECKROOM, message[2]);
                // projection 16
                System.out.println("proj16");
                message = space.get(new ActualField(message[2]), new FormalField(Boolean.class));
                // projection 17
                System.out.println("proj17");
                if ((boolean) message[1]) {
                    System.out.println("proj17then");
                    space.put(cToU, "then");
                    // projection 18
                    System.out.println("proj18");
                    space.put(cToU, ServerFlag.OK, message[0]);
                } else {
                    System.out.println("proj17else");
                    space.put(cToU, "else");
                    // projection 19
                    System.out.println("proj19");
                    space.put(cToU, ServerFlag.KO, message[0]);
                }
            }

//            while (true) {
//                Object[] message = space.get(initialMessageTemplate.getFields());
//                if (message[2].equals(ServerFlag.HOST)) {
//                    space.put(ServerFlag.GENERATEROOM,"");
//                    roomName = (String) space.get(new ActualField(ServerResponseFlag.GENERATEDROOM),new FormalField(String.class))[1];

//                    // get ok from room
//                    space.get(new ActualField(roomName),new ActualField(ServerFlag.ROOMOK));
//                    // send gate string to user
//                    System.out.println("Putting: "+playerID+" OK "+roomName);
//                    space.put(playerID, ServerFlag.OK,roomName);
//                    //set roomName to true
//                    space.put(ServerFlag.SETROOM,roomName);
//
//                    break;
//                } else { // Must be JOIN
//                    roomName = message[1].toString(); //update roomName to requested one
//                    space.put(ServerFlag.CHECKROOM,roomName);
//                    Object[] serverResponse = space.get(new ActualField(roomName),new FormalField(Boolean.class));
//                    System.out.println("room exists: " + serverResponse[1]);
//                    if ((boolean) serverResponse[1]) {
//                        space.put(playerID, ServerFlag.OK,roomName);
//                        System.out.println("joining room: " + roomName);
//                        break;
//                    }
//                    // Room does not exist
//                    space.put(playerID, ServerFlag.KO,"");
//                }
//            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void createRoom(String roomName) throws InterruptedException {
        new Thread(new Room(repository,roomName,space)).start();
        System.out.println("creatingRoom: " + roomName);


    }

}
