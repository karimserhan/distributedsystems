import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

/**
 * Created by Karim-pc1 on 9/13/2015.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        ServerSocket listener = new ServerSocket(6666);
        listener.setSoTimeout(Constants.TIMEOUT_INTERVAL);
        new Thread(new Runnable() {
            @Override
            public void run() {

                Socket clientSocket = null;
                try {
                    clientSocket = listener.accept();
                    clientSocket.setSoTimeout(5000);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                BufferedReader input = null;
                try {
                    input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    System.out.println("read: " + input.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        System.out.println("send to server: ");
        Scanner sc = new Scanner(System.in);

        Socket serverSocket = new Socket(
                InetAddress.getLocalHost(),
                6666);
        serverSocket.setSoTimeout(Constants.TIMEOUT_INTERVAL);

        PrintStream outToServer = new PrintStream(serverSocket.getOutputStream());
        outToServer.println(sc.nextLine());

        String filePath = args[0];
        FileReader inputFile = new FileReader(filePath);
        BufferedReader reader = new BufferedReader(inputFile);
        String line;

        try {
            while ((line = reader.readLine()) != null) {

                System.out.println(line);

            }
        }

        catch (IOException e){}

    }
}
