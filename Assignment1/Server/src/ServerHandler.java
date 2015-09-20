import java.io.*;
import java.net.*;
import java.util.ArrayList;

/**
 * Created by Karim-pc1 on 9/13/2015.
 */
public class ServerHandler {
    private int serverId;
    private int[] vectorClock;
    private int[] queue;
    private boolean[] isWriteRequestQueue;
    private boolean[] serverAvailability;
    private MachineAddress[] serverAddresses;
    private String[] reservations;
    private volatile int lastUpdateTime;
    private volatile boolean isInWriteCriticalSection;

    public ServerHandler(Server server) {
        this.serverId = server.getServerId();
        this.serverAddresses = server.getServerAddresses();
        this.reservations = server.getReservations();

        int numOfServers = serverAddresses.length;
        this.vectorClock = new int[numOfServers]; // defaults to zeros
        this.queue = new int[numOfServers];
        this.serverAvailability = new boolean[numOfServers]; // defaults to false
        this.isWriteRequestQueue = new boolean[numOfServers]; // defaults to false
        this.isInWriteCriticalSection = false;

        for (int i = 0; i < numOfServers; i++) {
            this.serverAvailability[i] = true;
            this.queue[i] = Integer.MAX_VALUE;
        }
    }

    public void listenForIncomingRequests(int port) {
        try {
            ServerSocket listener = new ServerSocket(port);
            while (true) {
                Socket incomingSocket = listener.accept();
                incomingSocket.setSoTimeout(Constants.TIMEOUT_INTERVAL);

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        handleServerRequest(incomingSocket);
                        try { incomingSocket.close(); }
                        catch (IOException exp) { Logger.debug(exp.getMessage()); }
                    }
                });
                thread.start();
            }
        }
        catch (IOException exp) {
            Logger.debug(exp.getMessage());
        }
    }

    ////////////////////////////////////////////////////////
    ////////////// Sending ooutgoing requests //////////////
    ////////////////////////////////////////////////////////
    /**
     * Requests a critical section from the other processes according
     * to Lamport's algorithm. Blocks until we can enter the C.S.
     */
    public void acquireCriticalSection(boolean isWriteRequest) {
        Logger.debug("Acquiring critical section...");

        // increment my clock
        vectorClock[serverId]++;

        if (isWriteRequest) {
            isInWriteCriticalSection = true;
        }

        // update queue
        queue[serverId] = vectorClock[serverId];

        // send request message to all servers
        for (int i = 0; i < serverAddresses.length; i++) {
            if (i != serverId && serverAvailability[i]) {
                Logger.debug("Requesting C.S. from server " + i);
                final int currentServerIndex = i;

                Thread connectionThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Socket serverSocket = new Socket(
                                    serverAddresses[currentServerIndex].getIpAddress(),
                                    serverAddresses[currentServerIndex].getServerPort());
                            serverSocket.setSoTimeout(Constants.TIMEOUT_INTERVAL);

                            BufferedReader inFromServer = new BufferedReader(
                                    new InputStreamReader(serverSocket.getInputStream()));
                            PrintStream outToServer = new PrintStream(serverSocket.getOutputStream());

                            // message format: <my_timestamp> <my_id> request_critical_section <read/write>
                            String message = vectorClock[serverId] + " " + serverId
                                    + " " + Constants.REQUEST_CS_COMMAND
                                    + " " + ((isWriteRequest == true) ? "write" : "read");

                            // send CS request
                            outToServer.println(message);
                            outToServer.flush();

                            // wait for ack and update clock vector
                            String ackBack = inFromServer.readLine();
                            Logger.debug("Got following response from server " + currentServerIndex + ":\n" + ackBack);
                            String[] ackBackData = ackBack.split(" ");

                            // close socket connection
                            serverSocket.close();

                            // first two fields are always timestamp and incoming server ID
                            int otherServerTime;
                            int otherServerId;
                            try {
                                otherServerTime = Integer.parseInt(ackBackData[0]);
                                otherServerId = Integer.parseInt(ackBackData[1]);
                            } catch (Exception exp) {
                                Logger.debug("Invalid message from server");
                                return;
                            }

                            syncClockVector(otherServerId, otherServerTime);
                        } catch (SocketTimeoutException exp) {
                            killServer(serverAddresses[currentServerIndex].getIpAddress());
                        } catch (ConnectException exp) {
                            killServer(serverAddresses[currentServerIndex].getIpAddress());
                            Logger.debug(exp.getMessage());
                        } catch (Exception exp) {
                            Logger.debug("Unexpected exception");
                            Logger.debug(exp.getMessage());
                            exp.printStackTrace();
                        }
                    }
                });
                connectionThread.start();
            }
            else if (i != serverId) {
                Logger.debug("Skipping server " + i + " because he's dead");
            }
        }

        // wait until we can enter the critical section
        Logger.debug("Waiting for critical section to be granted...");
        waitForAllServers(isWriteRequest);
        Logger.debug("Critical section has been granted");
    }

    public void releaseCriticalSection() {
        Logger.debug("Releasing critical section...");

        // increment my clock
        vectorClock[serverId]++;

        isInWriteCriticalSection = false;

        // update queue
        queue[serverId] = Integer.MAX_VALUE;

        // send release message to all servers
        for (int i = 0; i < serverAddresses.length; i++) {
            if (i != serverId && serverAvailability[i]) {
                Logger.debug("Sending release message to server " + i);
                Socket serverSocket = null;
                try {
                    serverSocket = new Socket(
                            serverAddresses[i].getIpAddress(),
                            serverAddresses[i].getServerPort());

                    PrintStream outToServer = new PrintStream(serverSocket.getOutputStream());

                    // message format: <my_timestamp> <my_id> request_critical_section <read/write>
                    String message = vectorClock[serverId] + " " + serverId
                            + " " + Constants.RELEASE_CS_COMMAND;

                    outToServer.println(message);

                    // close socket connection
                    serverSocket.close();
                } catch (ConnectException exp) {
                    killServer(serverAddresses[i].getIpAddress());
                    Logger.debug(exp.getMessage());
                } catch (Exception exp) {
                    Logger.debug("Unexpected exception");
                    Logger.debug(exp.getMessage());
                    exp.printStackTrace();
                }
            } else if (i != serverId) {
                Logger.debug("Skipping release message to server " + i + " because he's dead");
            }
        }

        Logger.debug("Done sending all release messages");
    }

    public void syncDataWithSquad() {
        Logger.debug("Sending the updated data to all other servers...");

        // increment my clock
        vectorClock[serverId]++;

        // keep track of all threads created to send sync request
        // so we can wait for them in the end
        ArrayList<Thread> outstandingThreads = new ArrayList<>();

        // send new data message to all servers
        for (int i = 0; i < serverAddresses.length; i++) {
            if (i != serverId && serverAvailability[i]) {
                Logger.debug("Sending to server " + i);
                final int currentServerIndex = i;

                Thread connectionThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Socket serverSocket = new Socket(
                                    serverAddresses[currentServerIndex].getIpAddress(),
                                    serverAddresses[currentServerIndex].getServerPort());
                            serverSocket.setSoTimeout(Constants.TIMEOUT_INTERVAL);

                            BufferedReader inFromServer = new BufferedReader(
                                    new InputStreamReader(serverSocket.getInputStream()));
                            PrintStream outToServer = new PrintStream(serverSocket.getOutputStream());

                            // message format: <my_timestamp> <my_id> sync_data \n <data>
                            String message = vectorClock[serverId] + " " + serverId
                                    + " " + Constants.SYNC_DATA_COMMAND
                                    + "\n" + serializeReservations();

                            // send CS request
                            outToServer.println(message);
                            outToServer.flush();

                            // wait for ack and update clock vector
                            String ackBack = inFromServer.readLine();
                            Logger.debug("Got following response from server " + currentServerIndex + ":\n" + ackBack);
                            String[] ackBackData = ackBack.split(" ");

                            // close socket connection
                            serverSocket.close();

                            // first two fields are always timestamp and incoming server ID
                            int otherServerTime;
                            int otherServerId;
                            try {
                                otherServerTime = Integer.parseInt(ackBackData[0]);
                                otherServerId = Integer.parseInt(ackBackData[1]);
                            } catch (Exception exp) {
                                Logger.debug("Invalid message from server");
                                return;
                            }

                            syncClockVector(otherServerId, otherServerTime);

                        } catch (SocketTimeoutException exp) {
                            killServer(serverAddresses[currentServerIndex].getIpAddress());
                        } catch (ConnectException exp) {
                            killServer(serverAddresses[currentServerIndex].getIpAddress());
                            Logger.debug(exp.getMessage());
                        } catch (Exception exp) {
                            Logger.debug("Unexpected exception");
                            Logger.debug(exp.getMessage());
                            exp.printStackTrace();
                        }
                    }
                });
                connectionThread.start();
                outstandingThreads.add(connectionThread);
            } else if (i != serverId) {
                Logger.debug("Skipping server " + i + " because he's dead");
            }
        }

        Logger.debug("Waiting for ack back from all servers");
        // wait for all outstanding threads
        for (Thread thread : outstandingThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Logger.debug("All ack backs have been received... Data is now synced with everyone");
    }

    public void joinSquad() {
        Logger.debug("Joining the server group");

        // increment my clock
        vectorClock[serverId]++;

        // send request to join message to all servers
        for (int i = 0; i < serverAddresses.length; i++) {
            if (i != serverId && serverAvailability[i]) {
                Logger.debug("Sending join request to server " + i);
                final int currentServerIndex = i;

                Thread connectionThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Socket serverSocket = new Socket(
                                    serverAddresses[currentServerIndex].getIpAddress(),
                                    serverAddresses[currentServerIndex].getServerPort());
                            serverSocket.setSoTimeout(Constants.TIMEOUT_INTERVAL);

                            BufferedReader inFromServer = new BufferedReader(
                                    new InputStreamReader(serverSocket.getInputStream()));
                            PrintStream outToServer = new PrintStream(serverSocket.getOutputStream());

                            // message format: <my_timestamp> <my_id> request_join_group
                            String message = vectorClock[serverId] + " " + serverId
                                    + " " + Constants.REQUEST_JOIN_COMMAND;

                            // send join request
                            outToServer.println(message);
                            outToServer.flush();

                            // wait for ack and update clock vector
                            String ackBack = inFromServer.readLine();
                            Logger.debug("Got following response from server " + currentServerIndex + ":\n" + ackBack);
                            String[] ackBackData = ackBack.split(" ");

                            String tableData = inFromServer.readLine();

                            // close socket connection
                            serverSocket.close();

                            // first two fields are always timestamp and incoming server ID
                            int otherServerTime;
                            int otherServerId;
                            try {
                                otherServerTime = Integer.parseInt(ackBackData[0]);
                                otherServerId = Integer.parseInt(ackBackData[1]);
                            } catch (Exception exp) {
                                Logger.debug("Invalid message from server");
                                return;
                            }

                            syncClockVector(otherServerId, otherServerTime);
                            updateReservations(tableData, otherServerTime);
                        } catch (SocketTimeoutException exp) {
                            killServer(serverAddresses[currentServerIndex].getIpAddress());
                        } catch (ConnectException exp) {
                            killServer(serverAddresses[currentServerIndex].getIpAddress());
                            Logger.debug(exp.getMessage());
                        } catch (Exception exp) {
                            Logger.debug("Unexpected exception");
                            Logger.debug(exp.getMessage());
                            exp.printStackTrace();
                        }
                    }
                });
                connectionThread.start();
            } else if (i != serverId) {
                Logger.debug("Skipping server " + i + " because he's dead");
            }
        }
    }

    /**
     * waits until all processes to return an ACK and until
     * this process is the smallest in the queue
     */
    private void waitForAllServers(boolean isWriteRequest) {
        boolean csConditionSatisfied = false;

        while (!csConditionSatisfied) {
            csConditionSatisfied = true;

            // check if C.S. condition
            for (int i = 0; i < queue.length; i++) {
                if (isWriteRequest) { // if this is a write request check for the
                    // standard lamport condition to enter C.S.
                    if (i != serverId && serverAvailability[i]) {
                        if (vectorClock[i] < queue[serverId] || queue[i] < queue[serverId]) {
                            csConditionSatisfied = false;
                        }
                        // matches are handled by a global order based on server index
                        if (queue[i] == queue[serverId] && i < serverId) {
                            csConditionSatisfied = false;
                        }
                    }
                } else { // if this is a read request then make sure there are no
                    // write requests in the queue with a smaller request time or
                    // smaller ack time.
                    if (i != serverId && serverAvailability[i]) {
                        if (vectorClock[i] < queue[serverId] ||
                                (queue[i] < queue[serverId] && isWriteRequestQueue[i])) {
                            csConditionSatisfied = false;
                        }
                        // matches are handled by a global order based on server index
                        if (queue[i] == queue[serverId] && i < serverId && isWriteRequestQueue[i]) {
                            csConditionSatisfied = false;
                        }
                    }
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

    private void killServer(InetAddress address) {
        int serverId = 0;
        for (MachineAddress addr : serverAddresses) {
            if (addr.equals(address)) {
                serverAvailability[serverId] = false;
                Logger.debug("Killing server " + serverId + " because he took too long to respond");
            }
        }
        serverId++;
    }

    ////////////////////////////////////////////////////////
    ////////////// Handling incoming requests //////////////
    ////////////////////////////////////////////////////////
    /**
     * Entry point of any incoming server request
     */
    public void handleServerRequest(Socket serverSocket) {
        try {
            Logger.debug("Incoming request received from server " + serverSocket.getInetAddress().getHostAddress());
            BufferedReader inFromServer = new BufferedReader(
                    new InputStreamReader(serverSocket.getInputStream()));
            String line = inFromServer.readLine();
            Logger.debug("Received following request form server:\n" + line);
            String[] serverData = line.split(" ");

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

            // keep baba alive
            serverAvailability[otherServerId] = true;

            // construct first part of response: <my_timestamp> <my_id>
            String outputStr = vectorClock[serverId] + " " + serverId + " ";

            if (serverData[2].equalsIgnoreCase(Constants.REQUEST_CS_COMMAND)) {
                Logger.debug("Server " + otherServerId + " is requesting critical section");
                boolean isWriteRequest = serverData[3].equalsIgnoreCase("write");

                // update queue for incoming process with its timestamp & request type
                queue[otherServerId] = otherServerTime;
                isWriteRequestQueue[otherServerId] = isWriteRequest;

                // construct ack to send back
                Logger.debug("Acking back for CS to server " + serverId);
                outputStr += Constants.ACK_CS_COMMAND;
            } else if (serverData[2].equalsIgnoreCase(Constants.RELEASE_CS_COMMAND)) {
                Logger.debug("Server " + otherServerId + " is releasing critical section");
                // update queue for incoming processwith value infinity
                queue[otherServerId] = Integer.MAX_VALUE;

                // don't send back anything
                return;
            } else if (serverData[2].equalsIgnoreCase(Constants.REQUEST_JOIN_COMMAND)) {
                Logger.debug("Server " + otherServerId + " is requesting to join the group");
                Logger.debug("Replying to server " + otherServerId + " with my data");
                outputStr += "\n" + serializeReservations();
            } else if (serverData[2].equalsIgnoreCase(Constants.SYNC_DATA_COMMAND)) {
                Logger.debug("Server " + otherServerId + " is sending his data to sync");
                String serializedData = inFromServer.readLine();
                updateReservations(serializedData, otherServerTime);
                // we need to ack back to ensure release is not sent before
                // everyone synced their data -- cuz otherwise we can serve a client
                // with outdated data (since it's a multithreaded server)
                Logger.debug("Acking back to sync command to server " + otherServerId);
                outputStr += Constants.ACK_SYNC_COMMAND;
            } else {
                Logger.debug("Invalid message from server");
                return;
            }

            PrintWriter outToServer = new PrintWriter(serverSocket.getOutputStream());
            outToServer.println(outputStr);
            outToServer.flush();
        }
        catch (SocketTimeoutException exp) {
            killServer(serverSocket.getInetAddress());
        }
        catch (IOException exp) {
            killServer(serverSocket.getInetAddress());
            Logger.debug(exp.getMessage());
        }
    }

    private void syncClockVector(int otherServerId, int otherServerTime) {
        vectorClock[serverId] = Math.max(vectorClock[serverId], otherServerTime) + 1;
        vectorClock[otherServerId] = Math.max(vectorClock[otherServerId], otherServerTime);
    }

    ////////////////////////////////////////////////////////
    ////////////// Common to incoming/outgoing /////////////
    ////////////////////////////////////////////////////////
    private String serializeReservations() {
        // serialize data
        String serializedData = "";
        String delimeter = "";
        for (String reservation : reservations) {
            serializedData += delimeter + ((reservation == null) ? "" : reservation);
            delimeter = "\t";
        }
        return serializedData;
    }

    private void updateReservations(String serializedData, int updateTime) {
        if (updateTime <= lastUpdateTime) {
            return;
        }

        String[] allReserervedNames = serializedData.split("\t",-1);
        if (allReserervedNames.length != reservations.length) {
            Logger.debug("Looks like a server has a non-consistent reservations table than me! GTFO");
            return;
        }
        int i =0;
        for (String reservationName : allReserervedNames) {
            if (!reservationName.equals("")) {
                reservations[i] = reservationName;
            } else {
                reservations[i] = null;
            }
            i++;
        }

        lastUpdateTime = updateTime;
    }

}
