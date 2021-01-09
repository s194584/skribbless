package common.src.main.Client;

import org.jspace.Space;

public class TaskInfo {
    String name;
    int userID;
    Space lobby;

    public TaskInfo(String n, int uID, Space l){
        name = n;
        userID = uID;
        lobby = l;
    }

    public int getUserID() {
        return userID;
    }

    public Space getLobby() {
        return lobby;
    }

    public String getName() {
        return name;
    }
}
