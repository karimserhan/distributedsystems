import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by Karim-pc1 on 9/13/2015.
 */
public class ClientHandler {
    Server server;

    public ClientHandler(Server server) {
        this.server = server;
    }

    public void handleClient(Socket clientSocket) {
        try {
            BufferedReader inFromClient = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            String[] clientData = inFromClient.readLine().split(" ");

            String outputStr;

            if (clientData.length < 2) {
                outputStr = "Invalid command from client";
                return;
            }

            if (clientData[0].equalsIgnoreCase(Constants.RESERVE_COMMAND)) {
                int count;
                try {
                    count = Integer.parseInt(clientData[2]);
                } catch (Exception e) {
                    Logger.debug(e.getMessage());
                    return;
                }
                outputStr = handleReserveRequest(clientData[1], count);
            } else if (clientData[0].equalsIgnoreCase(Constants.SEARCH_COMMAND)) {
                outputStr = handleSearchRequest(clientData[1]);
            } else if (clientData[0].equalsIgnoreCase(Constants.DELETE_COMMAND)) {
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

    private String handleReserveRequest(String name, int count) {
        this.server.getServerHandler().requestCriticalSection();
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
}
