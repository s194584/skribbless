package common.src.main.DataTransfer;

import org.jspace.Space;

/**
 *   Data Transfer object used when moving from initial logic to game logic
 */

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
