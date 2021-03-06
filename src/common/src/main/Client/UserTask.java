package common.src.main.Client;


import common.src.main.DataTransfer.TaskInfo;
import javafx.concurrent.Task;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;

import java.io.IOException;

import common.src.main.Enum.*;
import org.jspace.Space;

/**
 * The initial task responsible for communicating with the Server and CreationHandler upon launch, and communicating
 * changes to the StartController through bindings.
 * It is implemented in accordance with the protocol described in the report.
 */

public class UserTask extends Task {
    protected String name;
    protected int id;

    protected String hostPort;

    RemoteSpace serverSpace;
    protected Space lobby;
    protected Space ui;
    private boolean connection;

    public UserTask(Space ui) {
        this.ui = ui;

    }

    public TaskInfo getTaskInfo() {
        return new TaskInfo(name, id, lobby, hostPort);
    }

    @Override
    protected Integer call() throws Exception {
        while (true) {
            if (isCancelled()) {
                return 1;
            }

            // Information from ui
            hostPort = ui.get(new ActualField(UiFlag.IP), new FormalField(String.class))[1].toString();
            name = (String) ui.get(new ActualField(UiFlag.NAME), new FormalField(String.class))[1];
            String roomName = (String) ui.get(new ActualField(UiFlag.ROOMNAME), new FormalField(String.class))[1];
            ServerFlag action = (ServerFlag) ui.get(new ActualField(UiFlag.ACTION), new FormalField(ServerFlag.class))[1];

            // Helper
            Object[] response;

            // Projection 1
            connect();
            // Projection 2
            if (isConnected()) {
                // Projection 3
                serverSpace.put(ServerFlag.CONNECTED, "");
                // Projection 4
                id = (int) serverSpace.get(new FormalField(Integer.class))[0];

                // Shorthand
                ActualField cToU = new ActualField("creation" + id + "user");
                String uToC = "user" + id + "creation";

                // Projection 7
                serverSpace.put(uToC, action, roomName);
                // Projection 8
                String branch = (String) serverSpace.get(cToU, new FormalField(String.class))[1];
                if (branch.equals("then")) {
                    // Projection 15
                    response = serverSpace.get(cToU, new FormalField(Boolean.class), new FormalField(String.class));
                } else {
                    // Projection 18
                    response = serverSpace.get(cToU, new FormalField(Boolean.class), new FormalField(String.class));
                }
                // Projection 19
                if ((Boolean) response[1]) {
                    // Projection 20
                    connectToRoom((String) response[2]);
                    // Projection 21
                    connected();
                } else {
                    // Projection 22
                    notConnected();
                }
            } else {
                // Projection 23
                notConnected();
            }
        }
    }

    private void notConnected() throws InterruptedException {
        updateMessage("NOTCONNECTED");
        ui.getAll(new FormalField(UiFlag.class), new FormalField(Object.class));
    }

    private void connected() {
        updateMessage("CONNECTED");
    }

    private void connectToRoom(String o) throws IOException {
        lobby = new RemoteSpace(makeUri(o));
    }

    private boolean isConnected() {
        return connection;
    }

    private void connect() {
        try {
            serverSpace = new RemoteSpace(makeUri("serverSpace"));
            connection = true;
        } catch (Exception e) {
            connection = false;
            System.out.println("Can't find server");
        }
    }

    private String makeUri(String identifier) {
        return "tcp://" + hostPort + "/" + identifier + "?keep";
    }

}

