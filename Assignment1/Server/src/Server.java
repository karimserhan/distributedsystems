import javax.crypto.Mac;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Karim-pc1 on 9/13/2015.
 */
public class Server {

    private static final String RESERVE_COMMAND = "reserve";
    private static final String DELETE_COMMAND = "delete";
    private static final String SEARCH_COMMAND = "search";
    private static final String REQUEST_CS_COMMAND = "request_critical_section";
    private static final String ACK_CS_COMMAND = "ack_critical_section";
    private static final String RELEASE_CS_COMMAND = "release_critical_section";
    private static final long TIMEOUT_INTERVAL = 5000;

    private class MachineAddress {
        InetAddress ipAddress;
        int port;
    }

    private int serverId;
    private int[] vectorClock;
    private int[] queue;
    private boolean[] serverAvailability;
    private MachineAddress[] serverAddresses;
    private String[] reservations;

    private void listenForIncomingRequests() {
        try {
            ServerSocket listener = new ServerSocket(serverAddresses[serverId].port);
            while (true) {
                Socket incomingSocket = listener.accept();
                if (isServer(incomingSocket.getPort())) {

                } else{
                    handleClient(incomingSocket);
                    incomingSocket.close();
                }
            }
        }
        catch (IOException exp) {
            Logger.debug(exp.getMessage());
        }
    }

    private boolean isServer(int port) {
        for (MachineAddress serverAddress :  serverAddresses) {
            if (serverAddress.port == port) {
                return true;
            }
        }

        return false;
    }

    private void handleClient(Socket clientSocket) {
        try {
            BufferedReader inFromClient = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            String[] clientData = inFromClient.readLine().split(" ");

            String outputStr;

            if (clientData.length < 2) {
                outputStr = "Invalid command from client";
                return;
            }

            if (clientData[0].equalsIgnoreCase(RESERVE_COMMAND)) {
                int count;
                try {
                    count = Integer.parseInt(clientData[2]);
                } catch (Exception e) {
                    Logger.debug(e.getMessage());
                    return;
                }
                outputStr = handleReserveRequest(clientData[1], count);
            } else if (clientData[0].equalsIgnoreCase(SEARCH_COMMAND)) {
                outputStr = handleSearchRequest(clientData[1]);
            } else if (clientData[0].equalsIgnoreCase(DELETE_COMMAND)) {
                outputStr = handleDeleteRequest(clientData[1]);
            } else {
                outputStr = "Invalid command from client";
            }

            PrintWriter outToClient = new PrintWriter(clientSocket.getOutputStream());
            outToClient.println(outputStr);
            outToClient.flush();
        }
        catch (IOException exp) {
            Logger.debug(exp.getMessage());
        }
    }

    private void loadConfig(String fileName) {

    }

    private String handleReserveRequest(String name, int count) {
        // wait to acquire CS
        // output_msg = update reservations
        // release
        // send reservations to all
        // send output_msg to client
        return null;
    }

    private String handleDeleteRequest(String name) {
        return null;
    }

    private String handleSearchRequest(String name) {
        return null;
    }

    private void requestCriticalSection() {
        // update queue
        queue[serverId] = vectorClock[serverId];

        // send request message to all servers
        for (int i = 0; i < serverAddresses.length; i++) {
            if (i != serverId && serverAvailability[i]) {

                final MachineAddress currentServerAddress = serverAddresses[i];

                Thread connectionThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Socket serverSocket = new Socket(
                                    currentServerAddress.ipAddress,
                                    currentServerAddress.port);
                            String message = vectorClock[serverId] + " " + REQUEST_CS_COMMAND;
                            BufferedReader inFromServer = new BufferedReader(
                                    new InputStreamReader(serverSocket.getInputStream()));
                            PrintStream outToServer = new PrintStream(serverSocket.getOutputStream());

                            // send CS request
                            long startTime = System.currentTimeMillis();
                            outToServer.println(message);
                            outToServer.flush();

                            String ackBack = readLineWithTimeout(inFromServer);
                        } catch (IOException exp) {
                            Logger.debug(exp.getMessage());
                        }
                    }
                });
            }
        }
    }

    private String readLineWithTimeout(BufferedReader inputStream) throws IOException {
        long startTime = System.currentTimeMillis();

        final boolean[] lineWasRead = new boolean[1];
        lineWasRead[0] = false;

        Thread waitForTimeout = new Thread(new Runnable() {
            @Override
            public void run() {
                long currentTime = System.nanoTime();

                while (!lineWasRead[0] && (currentTime - startTime) < TIMEOUT_INTERVAL) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    currentTime = System.currentTimeMillis();
                }
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        waitForTimeout.start();

        String lineRead = null;

        lineRead = inputStream.readLine();

        lineWasRead[0] = true;

        return lineRead;
    }
}
