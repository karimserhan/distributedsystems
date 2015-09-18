import java.io.*;
import java.net.Socket;

/**
 * Created by Karim-pc1 on 9/13/2015.
 */
public class ServerHandler {
    private static volatile boolean[] inputProcessed;
    private int serverId;
    private int[] vectorClock;
    private int[] queue;
    private boolean[] serverAvailability;
    private MachineAddress[] serverAddresses;
    private String[] reservations;

    public ServerHandler(Server server) {
        this.serverId = server.getServerId();
        this.serverAddresses = server.getServerAddresses();
        this.reservations = server.getReservations();
        int numOfServers = serverAddresses.length;
        this.vectorClock = new int[numOfServers]; // defaults to zeros
        this.queue = new int[numOfServers]; // defaults to zeros
        this.serverAvailability = new boolean[numOfServers]; // defaults to false
    }

    /**
     * Requests a critical section from the other processes according
     * to Lamport's algorithm. Blocks until we can enter the C.S.
     */
    public void requestCriticalSection() {
        resetServerTimeouts();

        // update queue
        queue[serverId] = vectorClock[serverId];

        // send request message to all servers
        for (int i = 0; i < serverAddresses.length; i++) {
            if (i != serverId && serverAvailability[i]) {
                final int currentServerIndex = i;

                Thread connectionThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Socket serverSocket = new Socket(
                                    serverAddresses[currentServerIndex].getIpAddress(),
                                    serverAddresses[currentServerIndex].getPort());
                            serverSocket.setSoTimeout(Constants.TIMEOUT_INTERVAL);

                            BufferedReader inFromServer = new BufferedReader(
                                    new InputStreamReader(serverSocket.getInputStream()));
                            PrintStream outToServer = new PrintStream(serverSocket.getOutputStream());

                            // message format: <my_timestamp> <my_id> request_critical_section
                            String message = vectorClock[serverId] + " " + serverId
                                    + " " + Constants.REQUEST_CS_COMMAND;

                            // send CS request
                            outToServer.println(message);
                            outToServer.flush();

                            String ackBack = inFromServer.readLine();
                            //Update vector clock from servers
                        } catch (IOException exp) {
                            Logger.debug(exp.getMessage());
                        }
                    }
                });
                connectionThread.start();
            }
        }

        // wait until we can enter the critical section
        waitForAllServers();
    }

    /**
     * Resets the inputProcessed flags to false. Typically called
     * before talking to other processes.
     */
    private void resetServerTimeouts() {
        for (int i = 0; i < inputProcessed.length; i++) {
            inputProcessed[i] = false;
        }
    }

    /**
     * waits until all processes to return an ACK and until
     * this process is the smallest in the queue
     */
    private void waitForAllServers() {
        boolean csConditionSatisfied = false;

        while (!csConditionSatisfied) {
            csConditionSatisfied = true;

            // check if C.S. condition
            for (int i = 0; i < queue.length; i++) {
                if (i != serverId && serverAvailability[i]
                        && queue[i] < queue[serverId]
                        && vectorClock[i] < queue[serverId]) {
                    csConditionSatisfied = false;
                }
            }

            // if we can't enter the critical section, sleep for a bit
            if (!csConditionSatisfied) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Logger.debug(e.getMessage());
                }
            }
        }
    }

    /**
     * Entry point of any incoming server request
     */
    public void handleServerRequest(Socket serverSocket) {
        try {
            BufferedReader inFromServer = new BufferedReader(
                    new InputStreamReader(serverSocket.getInputStream()));
            String[] serverData = inFromServer.readLine().split(" ");

            // first two fields are always timestamp and incoming server ID
            int otherServerTime;
            int otherServerId;
            try {
                otherServerTime = Integer.parseInt(serverData[0]);
                otherServerId = Integer.parseInt(serverData[1]);
            } catch (Exception exp) {
                Logger.debug("Invalid message from server");
                return;
            }

            syncClockVector(otherServerId, otherServerTime);

            // construct first part of response: <my_timestamp> <my_id>
            String outputStr = vectorClock[serverId] + " " + serverId + " ";

            if (serverData[1].equalsIgnoreCase(Constants.REQUEST_CS_COMMAND)) {
                // update queue for incoming process with its timestamp
                queue[otherServerId] = otherServerTime;

                // construct ack to send back
                outputStr += Constants.ACK_CS_COMMAND;
            } else if (serverData[0].equalsIgnoreCase(Constants.RELEASE_CS_COMMAND)) {
                // update queue for incoming processwith value infinity
                queue[otherServerId] = Integer.MAX_VALUE;

                // don't send back anything
                return;
            } else if (serverData[0].equalsIgnoreCase(Constants.REQUEST_JOIN_COMMAND)) {
                outputStr += handleJoinRequest();
            } else if (serverData[0].equalsIgnoreCase(Constants.SYNC_DATA_COMMAND)) {
                String serializedData = inFromServer.readLine();
                handleSyncRequest(serializedData);
                outputStr += Constants.ACK_SYNC_COMMAND;
            } else {
                Logger.debug("Invalid message from server");
                return;
            }

            PrintWriter outToServer = new PrintWriter(serverSocket.getOutputStream());
            outToServer.println(outputStr);
            outToServer.flush();
        }
        catch (IOException exp) {
            Logger.debug(exp.getMessage());
        }
    }

    private String handleJoinRequest() {
        // TODO: implement
        return null;
    }

    private void handleSyncRequest(String serializedData) {
        // TODO: implement
    }

    private void syncClockVector(int otherServerId, int otherServerTime) {
        vectorClock[serverId] = Math.max(vectorClock[serverId], otherServerTime) + 1;
        vectorClock[otherServerId] = Math.max(vectorClock[otherServerId], otherServerTime);
    }
}
