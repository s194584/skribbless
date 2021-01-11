package common.src.main.Client;

import common.src.main.Enum.RoomFlag;
import javafx.concurrent.Task;
import org.jspace.Space;

public class GameUserTask extends Task {
    User user;
    int userId;



    Space lobby;
    Space ui;

    public GameUserTask(TaskInfo ti, Space ui){
        user = new User(ti.getName()+":D",ti.getName(),0);
        userId = ti.getUserID();
        lobby = ti.getLobby();
        this.ui = ui;
    }


    @Override
    protected Integer call() throws Exception {
        if (isCancelled())
            return -1;

        lobby.put(RoomFlag.CONNECTED,userId,user);

        return 1;
    }
}
