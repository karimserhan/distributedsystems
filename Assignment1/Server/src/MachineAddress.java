import java.net.InetAddress;

public class MachineAddress {
    private InetAddress ipAddress;
    private int clientPort;
    private int serverPort;

    public MachineAddress(InetAddress ipAddress, int clientPort, int serverPort) {
        this.ipAddress = ipAddress;
        this.clientPort = clientPort;
        this.serverPort = serverPort;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public int getClientPort()
    {
        return clientPort;
    }

    public int getServerPort()
    {
        return serverPort;
    }


}