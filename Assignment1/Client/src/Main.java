import javax.swing.plaf.synth.SynthEditorPaneUI;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by egantoun on 9/17/15.
 */
public class Main {

    private static  MachineAddress[] serverAddresses;
    private static List<MachineAddress> aliveServers;


    public static void main(String[] args)
    {
        if (args.length < 1) {
            System.out.println("Provide file name as cmd arg");
            return;
        }

        loadConfig(args[0]);

        while(true)
        {
            System.out.println("Enter Command: \n");
            Scanner sc = new Scanner(System.in);
            String command = sc.nextLine();

            if(command.equals("quit"))
            {
                break;
            }

            aliveServers  = new LinkedList<MachineAddress>();
            for(int i =0; i<serverAddresses.length;i++)
            {
                aliveServers.add(i, serverAddresses[i]);
            }

            handleSingleCommand(command);
        }
    }

    private static boolean handleSingleCommand(String command)
    {
        while(true)
        {
            Random rand = new Random();
            int randNumber = rand.nextInt(aliveServers.size());
            InetAddress serverIpAddress = (aliveServers.get(randNumber)).getIpAddress();
            int clientPort = (aliveServers.get(randNumber)).getClientPort();

            try {
                Socket serverSocket = new Socket(serverIpAddress, clientPort);

                serverSocket.setSoTimeout(Constants.TIMEOUT_INTERVAL);

                BufferedReader inFromServer = new BufferedReader(
                        new InputStreamReader(serverSocket.getInputStream()));
                PrintStream outToServer = new PrintStream(serverSocket.getOutputStream());

                outToServer.println(command);
                outToServer.flush();

                System.out.println(inFromServer.readLine());
                serverSocket.close();
                return true;
                
            }


            catch(Exception e)
            {
                aliveServers.remove(randNumber);

                if(aliveServers.size() ==0)
                {
                    System.out.println("Servers not Available");
                    return false;
                }
            }
        }
    }

    private static void loadConfig(String fileName)  {

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

}
