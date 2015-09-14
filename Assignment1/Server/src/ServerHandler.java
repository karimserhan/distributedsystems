import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
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

    public void requestCriticalSection() {
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
                            String message = vectorClock[serverId] + " " + Constants.REQUEST_CS_COMMAND;
                            BufferedReader inFromServer = new BufferedReader(
                                    new InputStreamReader(serverSocket.getInputStream()));
                            PrintStream outToServer = new PrintStream(serverSocket.getOutputStream());

                            // send CS request
                            long startTime = System.currentTimeMillis();
                            outToServer.println(message);
                            outToServer.flush();

                            String ackBack = readLineWithTimeout(inFromServer, currentServerIndex);
                        } catch (IOException exp) {
                            Logger.debug(exp.getMessage());
                        }
                    }
                });
            }
        }

        waitForAllServers();
    }

    private String readLineWithTimeout(BufferedReader inputStream, int serverIndex) throws IOException {
        long startTime = System.currentTimeMillis();

        inputProcessed[serverIndex] = false;

        Thread waitForTimeout = new Thread(new Runnable() {
            @Override
            public void run() {
                long currentTime = System.nanoTime();

                while (!inputProcessed[serverIndex] && (currentTime - startTime) < Constants.TIMEOUT_INTERVAL) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    currentTime = System.currentTimeMillis();
                }
                // server didn't reply within timeout
                // 1. Kill server
                serverAvailability[serverIndex] = false;

                // 2. close stream
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // 3. set flag
                inputProcessed[serverIndex] = true;
            }
        });

        waitForTimeout.start();
        String lineRead = null;
        lineRead = inputStream.readLine();
        inputProcessed[serverIndex] = true;

        return lineRead;
    }

    private void waitForAllServers() {
        boolean allInputProcessed = false;
        while (!allInputProcessed) {
            allInputProcessed = true;
            for (boolean oneInput : inputProcessed) {
                if (!oneInput) {
                    allInputProcessed = false;
                    break;
                }
            }

            if (!allInputProcessed) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                break;
            }
        }
    }
}
