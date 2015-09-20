import org.omg.CORBA.MARSHAL;

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

    private void listenForIncomingRequests() {
        try {
            ServerSocket listener = new ServerSocket(serverAddresses[serverId].getPort());
            listener.setSoTimeout(Constants.TIMEOUT_INTERVAL);
            while (true) {
                Socket incomingSocket = listener.accept();
                if (isServer(incomingSocket)) {
                    serverHandler.handleServerRequest(incomingSocket);
                } else{
                    clientHandler.handleClientRequest(incomingSocket);
                }
                incomingSocket.close();
            }
        }
        catch (IOException exp) {
            Logger.debug(exp.getMessage());
        }
    }

    private boolean isServer(Socket serverSocket) {
        for (MachineAddress serverAddress :  serverAddresses) {
            if (serverAddress.getIpAddress().equals(serverSocket.getInetAddress())
                    && serverAddress.getPort() == serverSocket.getPort()) {
                return true;
            }
        }

        return false;
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
                int portAddress = Integer.parseInt(lineContents[1]);
                MachineAddress address = new MachineAddress(serverIpAddress, portAddress);
                machineAddressesInConfigFile.add(tempServerIndex, address);
                tempServerIndex++;
            }

            serverAddresses = new MachineAddress[machineAddressesInConfigFile.size()];
            machineAddressesInConfigFile.toArray(serverAddresses);
        }

        catch (IOException e){
            Logger.debug(e.getMessage());
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

    public void requestCriticalSection() {
        serverHandler.requestCriticalSection();
    }


}
