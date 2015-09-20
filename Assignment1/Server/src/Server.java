import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private ClientHandler clientHandler;
    private ServerHandler serverHandler;

    private int serverId;
    private MachineAddress[] serverAddresses;
    private String[] reservations;

    public Server(int serverId, int nbrOfSeats, String configFileName) {
        // set up logger
        Logger.serverIndex = serverId;

        loadConfig(configFileName);
        this.serverId = serverId;
        this.reservations = new String[nbrOfSeats];

        this.clientHandler = new ClientHandler(this);
        this.serverHandler = new ServerHandler(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                clientHandler.listenForIncomingRequests(
                        serverAddresses[serverId].getClientPort());
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                serverHandler.listenForIncomingRequests(
                        serverAddresses[serverId].getServerPort());
            }
        }).start();

        this.serverHandler.joinSquad();
    }

    private void loadConfig(String fileName)  {

        try
        {
            FileReader inputFile = new FileReader(fileName);
            BufferedReader reader = new BufferedReader(inputFile);
            String line;

            List<MachineAddress> machineAddressesInConfigFile = new ArrayList<MachineAddress>();
            int tempServerIndex = 0;
            while ((line = reader.readLine()) != null) {

                String[] lineContents = line.split(" ");
                InetAddress serverIpAddress = InetAddress.getByName(lineContents[0]);
                int clientPort = Integer.parseInt(lineContents[1]);
                int serverPort = Integer.parseInt(lineContents[2]);
                MachineAddress address = new MachineAddress(serverIpAddress, clientPort,serverPort);
                machineAddressesInConfigFile.add(tempServerIndex, address);
                tempServerIndex++;
            }

            serverAddresses = new MachineAddress[machineAddressesInConfigFile.size()];
            machineAddressesInConfigFile.toArray(serverAddresses);
        }

        catch (IOException e){

            System.out.println(e.getMessage());
        }


    }

    // Getters
    public ServerHandler getServerHandler() {
        return serverHandler;
    }

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
