package common.src.main.Client;

import common.src.main.Enum.RoomFlag;
import common.src.main.Enum.RoomResponseFlag;
import javafx.concurrent.Task;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.Space;

public class GameUserTask extends Task {
    User user;
    int userId;
    protected String hostPort;
    protected boolean isLeader;

    protected Space inbox;
    Space lobby;
    Space ui;

    public GameUserTask(TaskInfo ti, Space ui) {
        user = new User(ti.getName() + ":D", ti.getName(), 0);
        userId = ti.getUserID();
        lobby = ti.getLobby();
        hostPort = ti.getHostPort();
        this.ui = ui;
    }


    @Override
    protected Integer call() throws Exception {
        lobby.put(RoomFlag.CONNECTED, userId, user);
        Object[] roomResponse = lobby.get(new ActualField(userId), new FormalField(String.class), new FormalField(Boolean.class));
        System.out.println("Got response from room:" + roomResponse[1] + " isleader: " + roomResponse[2]);
        inbox = new RemoteSpace(makeUri(roomResponse[1].toString()));
        this.isLeader = (boolean) roomResponse[2];

        //send isLeader to GameController
        updateMessage("" + isLeader);

        //Make uiInbox thread.
        new Thread(new UiInbox(ui, lobby)).start();

        // GameUserTask now becomes an inbox and reads the inbox and notifies ui (GameController).
        while (true) {
            if (isCancelled()) {
                //TODO: This is never actually done.
                lobby.put(RoomFlag.DISCONNECTED,userId,user);
                return -1;
            }

            Object[] message = inbox.get(new FormalField(RoomResponseFlag.class), new FormalField(Object.class));
            updateValue(message);
        }
    }

    private String makeUri(String identifier) {
        return "tcp://" + hostPort + "/" + identifier + "?keep";
    }
}

class UiInbox implements Runnable {
    private Space uiSpace;
    private Space lobby;


    public UiInbox(Space uiSpace, Space lobby) {
        this.uiSpace = uiSpace;
        this.lobby = lobby;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Object[] message = uiSpace.get(new FormalField(RoomFlag.class),
                        new FormalField(Integer.class),
                        new FormalField(Object.class));
                lobby.put(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
