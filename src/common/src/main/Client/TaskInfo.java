package common.src.main.Client;

import org.jspace.Space;

public class TaskInfo {
    private String name;
    private int userID;
    private Space lobby;
    private String hostPort;

    public TaskInfo(String n, int uID, Space l,String hostPort){
        name = n;
        userID = uID;
        lobby = l;
        this.hostPort = hostPort;
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

    public String getHostPort() {
        return hostPort;
    }
}
