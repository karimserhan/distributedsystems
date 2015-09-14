import java.net.InetAddress;

public class MachineAddress {
    private InetAddress ipAddress;
    private int port;

    public MachineAddress(InetAddress ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }
}