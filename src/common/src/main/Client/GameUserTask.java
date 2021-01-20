package common.src.main.Client;

import common.src.main.DataTransfer.TaskInfo;
import common.src.main.DataTransfer.User;
import common.src.main.Enum.RoomFlag;
import common.src.main.Enum.RoomResponseFlag;
import javafx.concurrent.Task;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.Space;

/**
 * Initially responsible for communicating with the room thread, and retrieving the players inbox.
 * After this the thread becomes an entry point for messages from the Room to the inbox which are then parsed through
 * the update value method as to not block the JavaFx thread (GameController).
 */

public class GameUserTask extends Task {
    User user;
    int userId;
    protected String hostPort;
    protected boolean isLeader;

    protected Space inbox;
    Space lobby;

    public GameUserTask(TaskInfo ti) {
        user = new User(ti.getName() + ":D", ti.getName(), ti.getUserID(), 0);
        userId = ti.getUserID();
        lobby = ti.getLobby();
        hostPort = ti.getHostPort();
    }

    @Override
    protected Integer call() throws Exception {
        // Connect to room
        lobby.put(RoomFlag.CONNECTED, userId, user);
        Object[] roomResponse = lobby.get(new ActualField(userId), new FormalField(String.class), new FormalField(Boolean.class));
        // Link the inbox
        inbox = new RemoteSpace(makeUri(roomResponse[1].toString()));

        // Set leader status
        this.isLeader = (boolean) roomResponse[2];

        // Send the room name to controller
        int div = roomResponse[1].toString().indexOf("-");
        Object[] roomName = {RoomResponseFlag.ROOMNAME,roomResponse[1].toString().substring(0,div)};
        updateValue(roomName);

        // Send isLeader to GameController
        updateMessage("" + isLeader);

        // GameUserTask now becomes an inbox and reads the inbox and notifies ui (GameController).
        while (true) {
            if (isCancelled()) {
                return -1;
            }
            updateValue(inbox.get(new FormalField(RoomResponseFlag.class), new FormalField(Object.class)));
        }
    }

    // Handles if the user disconnects. The thread is then cancelled from the GameController.
    @Override
    protected void cancelled() {
        super.cancelled();
        try {
            lobby.put(RoomFlag.DISCONNECTED, userId, user);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String makeUri(String identifier) {
        return "tcp://" + hostPort + "/" + identifier + "?keep";
    }

}