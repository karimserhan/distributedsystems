import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private ClientHandler clientHandler;
    private ServerHandler serverHandler;

    private int serverId;
    private MachineAddress[] serverAddresses;
    private String[] reservations;

    private void listenForIncomingRequests() {
        try {
            ServerSocket listener = new ServerSocket(serverAddresses[serverId].getPort());
            while (true) {
                Socket incomingSocket = listener.accept();
                if (isServer(incomingSocket.getPort())) {

                } else{
                    clientHandler.handleClient(incomingSocket);
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
            if (serverAddress.getPort() == port) {
                return true;
            }
        }

        return false;
    }

    private void loadConfig(String fileName) {

    }

    // Getters
    public int getServerId() {
        return serverId;
    }

    public MachineAddress[] getServerAddresses() {
        return serverAddresses;
    }

    public String[] getReservations() {
        return reservations;
    }


}
