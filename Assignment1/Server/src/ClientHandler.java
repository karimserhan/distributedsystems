import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Karim-pc1 on 9/13/2015.
 */
public class ClientHandler {
    private Server server;
    private String[] reservations;
    private ReadWriteLock rwlock = new ReentrantReadWriteLock();

    public ClientHandler(Server server) {
        this.server = server;
        this.reservations = server.getReservations();
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
                        handleClientRequest(incomingSocket);
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

    public void handleClientRequest(Socket clientSocket) {
        Logger.debug("Incoming request received from client " + clientSocket.getInetAddress().getHostAddress());

        try {
            BufferedReader inFromClient = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            String[] clientData = inFromClient.readLine().split(" ");

            String outputStr;

            if (clientData.length < 2) {
                Logger.debug("Received invalid message from the client");
                outputStr = "Invalid command from client";
            } else if (clientData[0].equalsIgnoreCase(Constants.RESERVE_COMMAND)) {
                Logger.debug("Received reserve request from client");
                int count;
                try {
                    count = Integer.parseInt(clientData[2]);
                } catch (Exception e) {
                    Logger.debug(e.getMessage());
                    return;
                }
                outputStr = handleReserveRequest(clientData[1], count);
            } else if (clientData[0].equalsIgnoreCase(Constants.SEARCH_COMMAND)) {
                Logger.debug("Received search request from client");
                outputStr = handleSearchRequest(clientData[1]);
            } else if (clientData[0].equalsIgnoreCase(Constants.DELETE_COMMAND)) {
                Logger.debug("Received delete request from client");
                outputStr = handleDeleteRequest(clientData[1]);
            } else {
                Logger.debug("Received invalid message from the client");
                outputStr = "Invalid command from client";
            }

            Logger.debug("Replying to client with following response: \n" + outputStr);
            PrintWriter outToClient = new PrintWriter(clientSocket.getOutputStream());
            outToClient.println(outputStr);
            outToClient.flush();
        }
        catch (IOException exp) {
            Logger.debug(exp.getMessage());
        }
    }

    private String handleReserveRequest(String name, int count) {
        rwlock.writeLock().lock();
        try {
            // wait to acquire CS
            this.server.getServerHandler().acquireCriticalSection(true);

            Logger.debug("Reserving " + count + " seats for " + name);
            String returnMsg;

            // check if we have count available entries, and no current reservations for this name
            int available = 0;
            String currentReservations = "";
            String delimiter = "";
            for (int i = 0; i < reservations.length; i++) {
                if (reservations[i] == null) { available++; }
                else if (reservations[i].equals(name)) {
                    currentReservations += delimiter + i;
                    delimiter = ", ";
                }
            }

            if (!currentReservations.equals("")) { // failed
                returnMsg = String.format("Failed: %s has booked the following seats: %s.", name, currentReservations);
            } else if (available < count) { // failed
                returnMsg = String.format("Failed: only %d seats available but %d seats are requested.",
                        available, count);
            } else { // succeeded: reserve count seats for this name
                String foundSeats = "";
                delimiter = "";
                for (int i = 0; i < reservations.length; i++) {
                    if (count == 0) { break; }
                    if (reservations[i] == null) {
                        reservations[i] = name;
                        foundSeats += delimiter + i;
                        delimiter = ", ";
                        count--;
                    }
                }
                returnMsg = String.format("The seats have been reserved from %s: %s.", name, foundSeats);
            }

            // tell my squad about new table
            this.server.getServerHandler().syncDataWithSquad();

            // send output_msg to client

            // release
            this.server.getServerHandler().releaseCriticalSection();
            return returnMsg;
        } finally {
            rwlock.writeLock().unlock();
        }
    }

    private String handleDeleteRequest(String name) {
        rwlock.writeLock().lock();
        try {
            // wait to acquire CS
            this.server.getServerHandler().acquireCriticalSection(true);

            Logger.debug("Deleting all seats for " + name);

            // empty the reservations
            int releasedSeats = 0, availableSeats = 0;
            for (int i = 0; i < reservations.length; i++) {
                if (reservations[i] == null) { availableSeats++; }
                else if (reservations[i].equals(name)) {
                    reservations[i] = null;
                    releasedSeats++;
                    availableSeats++;
                }
            }
            String returnMsg = String.format("%d seats have been released. %d seats are now available.",
                    releasedSeats, availableSeats);

            // tell my squad about new table
            this.server.getServerHandler().syncDataWithSquad();

            // send output_msg to client

            // release
            this.server.getServerHandler().releaseCriticalSection();
            return returnMsg;
        } finally {
            rwlock.writeLock().unlock();
        }
    }

    private String handleSearchRequest(String name) {
        rwlock.writeLock().lock();
        try {
            Logger.debug("Searching for seats for " + name);

            String foundSeats = "";
            String delimiter = "";
            for (int i = 0; i < reservations.length; i++) {
                if (reservations[i] != null && reservations[i].equals(name)) {
                    foundSeats += delimiter + i;
                    delimiter = ", ";
                }
            }
            String returnMsg;
            if (!foundSeats.equals("")) {
                returnMsg = String.format("%s has reserved the following seats: %s.",
                        name, foundSeats);
            } else {
                returnMsg = String.format("Failed: no reservation is made by %s.", name);
            }

            return returnMsg;
        } finally {
            rwlock.writeLock().unlock();
        }
    }
}
