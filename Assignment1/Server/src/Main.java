import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("Provide these cmd args: <config_file> <server_index> <nbr_of_seats>");
            return;
        }

        String configFile;
        int serverIndex, nbrOfSeats;
        try {
            configFile = args[0];
            serverIndex = Integer.parseInt(args[1]);
            nbrOfSeats = Integer.parseInt(args[2]);
        }catch(NumberFormatException exp) {
            System.out.println("Wrong cmd args format");
            return;
        }
        Server server = new Server(serverIndex, nbrOfSeats, configFile);
    }
}
