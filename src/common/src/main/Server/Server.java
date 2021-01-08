package common.src.main.Server;

import org.jspace.*;

import java.util.HashMap;
import common.src.main.Enum.*;

public class Server {

    protected static int roomAmount = 0;
    protected static HashMap<String,Room> rooms;

    public static void main(String[] args) {

        try {
            // Create a repository
            SpaceRepository repository = new SpaceRepository();

            // Create a local space for the chat messages
            SequentialSpace serverSpace = new PileSpace();

            // Add the space to the repository
            repository.add("serverSpace", serverSpace);

            String gateUri = "tcp://localhost:9001/?keep";
            System.out.println("Opening repository gate at " + gateUri + "...");
            repository.addGate(gateUri);

            //Waiting on game to start
            Template initialMessageTemplate = new Template (new FormalField(String.class),
                                                            new FormalField(InitialMessage.class));  //Get name,enum

            while (true) {
                //serverSpace.get(initialMessageTemplate); //TODO: fix
                Object[] message = serverSpace.get(new FormalField(String.class),
                                                    new FormalField(InitialMessage.class));
                if (message[1].equals(InitialMessage.HOST)) {
                    Room room = new Room(repository);
                    addRoom(room);
                    new Thread(room).start();
                } else { // Must be JOIN
                    Room room = rooms.get(message[0].toString());
                    if (room != null) {
                        serverSpace.put(InitialMessage.OK); //TODO: must have identifer with!
                        Space chat = room.getChat(); //TODO: This should be a string to connect to.
                        serverSpace.put(chat);
                    }
                    // Room does not exist
                }


            }


        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    private static void addRoom(Room room) {
        rooms.put(createRoomName(room), room);
    }

    private static String createRoomName(Room room) {
        return "room" + "-" + roomAmount;
    }


}

